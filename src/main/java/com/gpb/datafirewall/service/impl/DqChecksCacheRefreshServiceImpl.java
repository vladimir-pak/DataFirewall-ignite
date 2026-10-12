package com.gpb.datafirewall.service.impl;

import com.gpb.datafirewall.service.IgniteCacheService;
import com.gpb.datafirewall.service.KafkaProducerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.gpb.datafirewall.compile.RuleCompilerPipeline;
import com.gpb.datafirewall.model.CacheVersion;
import com.gpb.datafirewall.model.CacheVersionId;
import com.gpb.datafirewall.model.DqChecks;
import com.gpb.datafirewall.parser.SqlTextNormalizer;
import com.gpb.datafirewall.parser.JavaRuleGenerator;
import com.gpb.datafirewall.properties.Caches;
import com.gpb.datafirewall.repository.CacheVersionRepository;
import com.gpb.datafirewall.repository.DqChecksRepository;
import org.apache.ignite.client.ClientCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DqChecksCacheRefreshServiceImpl {

    private final DqChecksRepository dqChecksRepository;
    private final CacheVersionRepository cacheVersionRepository;
    private final IgniteCacheService igniteCacheService;
    private final KafkaProducerService kafkaProducerService;

    @Value ("${datafirewall.generated-rules.enabled:true}")
    private boolean generatedRulesEnabled;

    @Value("${datafirewall.generated-rules.output-dir:./generated-rules}")
    private String generatedRulesOutputDir;

    @Value("${datafirewall.generated-rules.compiler-threads:4}")
    private int compilerThreads;

    private Map<Integer, String> changedOrNewRulesSql = new LinkedHashMap<>();
    private Set<Integer> deletedRuleIds = new LinkedHashSet<>();
    private volatile Map<Integer, String> lastCompilationErrors = Map.of();

    @Transactional
    public void refreshCaches() {
        this.lastCompilationErrors = Map.of();

        // 1. Читаем всю таблицу
        List<DqChecks> rows = dqChecksRepository.findAll();

        // 2. Учитываем версию генератора: новый генератор требует нового bytecode,
        // даже если исходное SQL-выражение не изменилось.
        Map<Integer, String> dbHashes = rows.stream()
                .collect(Collectors.toMap(
                        DqChecks::getId,
                        row -> sha256Hex(JavaRuleGenerator.GENERATOR_VERSION + "\n"
                                + Objects.toString(row.getSqlQuery(), "")),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        // Дополнительно держим id -> sqlQuery, чтобы потом отдать на компиляцию
        Map<Integer, String> dbSqlById = rows.stream()
                .collect(Collectors.toMap(
                        DqChecks::getId,
                        row -> Objects.toString(row.getSqlQuery(), ""),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        // 3. Получаем/создаем dqchecks_hash
        ClientCache<Integer, String> hashCache =
                igniteCacheService.getOrCreateCacheByFullName(Caches.DQCHECKS_HASH_CACHE.getName());

        Map<Integer, String> cacheHashes =
                igniteCacheService.cacheExistsByFullName(Caches.DQCHECKS_HASH_CACHE.getName())
                        ? igniteCacheService.readSnapshotByFullName(Caches.DQCHECKS_HASH_CACHE.getName())
                        : new LinkedHashMap<>();

        // 4-5. Сравниваем, выделяем:
        // changedOrNewRulesSql -> новые или изменившиеся SQL
        // deletedRuleIds -> удалённые id
        this.changedOrNewRulesSql = new LinkedHashMap<>();
        this.deletedRuleIds = new LinkedHashSet<>();

        for (Map.Entry<Integer, String> entry : dbHashes.entrySet()) {
            Integer id = entry.getKey();
            String newHash = entry.getValue();
            String oldHash = cacheHashes.get(id);

            if (oldHash == null || !oldHash.equals(newHash)) {
                this.changedOrNewRulesSql.put(id, dbSqlById.get(id));
            }
        }
        log.info("Найдено {} измененных или новых проверок", this.changedOrNewRulesSql.size());

        for (Integer cachedId : cacheHashes.keySet()) {
            if (!dbHashes.containsKey(cachedId)) {
                this.deletedRuleIds.add(cachedId);
            }
        }
        log.info("Найдено {} удаленных проверок", this.deletedRuleIds.size());

        if (this.changedOrNewRulesSql.size() == 0 && this.deletedRuleIds.size() == 0) {
            return;
        }

        RuleCompilerPipeline ruleCompiler = new RuleCompilerPipeline(
            compilerThreads,
            generatedRulesEnabled,
            generatedRulesOutputDir           
        );

        Map<Integer, String> normalized = new LinkedHashMap<>();
        this.changedOrNewRulesSql.forEach((id, sql) -> normalized.put(id, SqlTextNormalizer.normalize(sql)));

        // 6. Компилируем только новые/изменённые правила
        Map<String, byte[]> compiledDelta;
        Map<Integer, String> compilationErrors;

        try {
            compiledDelta =
                    normalized.isEmpty()
                            ? Collections.emptyMap()
                            : ruleCompiler.process(normalized);
            compilationErrors = ruleCompiler.getCompilationErrors();
        } finally {
            ruleCompiler.shutdown();
        }

        Set<Integer> successfullyCompiledRuleIds =
                normalized.keySet().stream()
                        .filter(id -> compiledDelta.containsKey("Rule" + id))
                        .collect(
                                Collectors.toCollection(
                                        LinkedHashSet::new
                                )
                        );

        Set<Integer> failedRuleIds =
                new LinkedHashSet<>(normalized.keySet());

        failedRuleIds.removeAll(successfullyCompiledRuleIds);

        if (!failedRuleIds.isEmpty()) {
            Map<Integer, String> failedReasons = new TreeMap<>();
            for (Integer failedRuleId : failedRuleIds) {
                failedReasons.put(
                        failedRuleId,
                        compilationErrors.getOrDefault(failedRuleId, "Unknown compilation error")
                );
            }
            this.lastCompilationErrors = Collections.unmodifiableMap(failedReasons);

            log.error(
                    "Не удалось скомпилировать {} Rule: {}. "
                            + "Они не будут обновлены в snapshot; успешные Rule и удаления будут опубликованы. "
                            + "Для существующих ошибочных Rule сохранится bytecode из предыдущей версии. Причины: {}",
                    failedRuleIds.size(),
                    failedRuleIds,
                    failedReasons
            );
        }

        // Если не скомпилировалось ни одного изменения и удалений нет,
        // публиковать идентичную новую версию snapshot не требуется.
        // Хэши ошибочных Rule также не обновляем: следующий refresh повторит попытку.
        if (compiledDelta.isEmpty() && this.deletedRuleIds.isEmpty()) {
            log.warn(
                    "Snapshot не изменён: успешно скомпилированных Rule и удалений нет. "
                            + "Ошибочные Rule будут повторно обработаны при следующем refresh"
            );
            return;
        }

        // 7. Определяем текущую версию compiled_rules из БД
        CacheVersion currentVersionRow =
                cacheVersionRepository.findTopByIdCacheNameOrderByIdVersionDesc(Caches.COMPILED_RULES.getName())
                        .orElse(null);

        Integer currentVersion = currentVersionRow == null
                ? null
                : currentVersionRow.getId().getVersion();
        Integer newVersion = currentVersion == null ? 1 : currentVersion + 1;

        // 8. Собираем новый compiled_rules_<newVersion>
        ClientCache<String, byte[]> newCompiledCache =
                igniteCacheService.getOrCreateVersionedCache(Caches.COMPILED_RULES.getName(), newVersion);

        // если версия уже есть — очищаем перед заполнением
        newCompiledCache.clear();

        // если есть старая версия, копируем её целиком
        if (currentVersion != null
                && igniteCacheService.cacheExists(Caches.COMPILED_RULES.getName(), currentVersion)) {

            Map<String, byte[]> previousSnapshot =
                    igniteCacheService.readSnapshot(Caches.COMPILED_RULES.getName(), currentVersion);

            if (!previousSnapshot.isEmpty()) {
                newCompiledCache.putAll(previousSnapshot);
            }
        }

        // накатываем изменения
        if (!compiledDelta.isEmpty()) {
            newCompiledCache.putAll(compiledDelta);
        }

        // удаляем ключи, которых больше нет в БД
        for (Integer deletedId : this.deletedRuleIds) {
            newCompiledCache.remove(String.format("Rule%s", deletedId));
        }

        // 9. Фиксируем новую версию в БД
        CacheVersion newVersionRow = new CacheVersion(
                new CacheVersionId(Caches.COMPILED_RULES.getName(), newVersion)
        );
        cacheVersionRepository.save(newVersionRow);

        // 10. Обновляем hash только для успешно применённых изменений.
        // Для ошибочных новых Rule hash не создаётся, а для ошибочных изменённых
        // сохраняется предыдущий hash — это гарантирует повторную попытку компиляции.
        Map<Integer, String> updatedHashes = new LinkedHashMap<>(cacheHashes);
        for (Integer successfulId : successfullyCompiledRuleIds) {
            updatedHashes.put(successfulId, dbHashes.get(successfulId));
        }
        for (Integer deletedId : this.deletedRuleIds) {
            updatedHashes.remove(deletedId);
        }

        hashCache.clear();
        if (!updatedHashes.isEmpty()) {
            hashCache.putAll(updatedHashes);
        }

        // 11. Удаляем старые кэши, за исключением нового и предыдущего
        if (currentVersion != null) {
            destroyOldCaches(currentVersion, newVersion);
        }

        // 12. Отправляем событие об обновлении в Kafka
        kafkaProducerService.send(
                Caches.COMPILED_RULES.getName(),
                newVersion
        );

        log.info(
                "Опубликован snapshot compiled_rules_{}: успешно обновлено Rule={}, "
                        + "пропущено с ошибками={}, удалено={}",
                newVersion,
                successfullyCompiledRuleIds.size(),
                failedRuleIds.size(),
                this.deletedRuleIds.size()
        );
    }

    private void destroyOldCaches(int currentVersion, int newVersion) {
        String currentFullCacheName = String.format("%s_%s", Caches.COMPILED_RULES.getName(), currentVersion);
        String newFullCacheName = String.format("%s_%s", Caches.COMPILED_RULES.getName(), newVersion);
        String prefix = Caches.COMPILED_RULES.getName() + "_";

        for (String cacheName : igniteCacheService.getCacheNames()) {
            if (!cacheName.startsWith(prefix)) {
                continue;
            }

            String suffix = cacheName.substring(prefix.length());
            if (!suffix.matches("\\d+")) {
                continue;
            }

            if (!cacheName.equals(currentFullCacheName) && !cacheName.equals(newFullCacheName)) {
                igniteCacheService.destroyCacheByFullName(cacheName);
            }
        }
    }

    public Map<Integer, String> getChangedOrNewRulesSql() {
        return Collections.unmodifiableMap(changedOrNewRulesSql);
    }

    public Set<Integer> getDeletedRuleIds() {
        return Collections.unmodifiableSet(deletedRuleIds);
    }

    public Map<Integer, String> getLastCompilationErrors() {
        return lastCompilationErrors;
    }

    private String sha256Hex(String source) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(
                    source == null ? new byte[0] : source.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate SHA-256", e);
        }
    }
}
