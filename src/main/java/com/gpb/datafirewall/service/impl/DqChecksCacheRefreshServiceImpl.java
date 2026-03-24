package com.gpb.datafirewall.service.impl;

import com.gpb.datafirewall.service.IgniteCacheService;

import lombok.RequiredArgsConstructor;

import com.gpb.datafirewall.compile.RuleCompilerPipeline;
import com.gpb.datafirewall.model.CacheVersion;
import com.gpb.datafirewall.model.CacheVersionId;
import com.gpb.datafirewall.model.DqChecks;
import com.gpb.datafirewall.parser.SqlTextNormalizer;
import com.gpb.datafirewall.repository.CacheVersionRepository;
import com.gpb.datafirewall.repository.DqChecksRepository;
import org.apache.ignite.client.ClientCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DqChecksCacheRefreshServiceImpl {

    private static final String DQCHECKS_HASH_CACHE = "dqchecks_hash";
    private static final String COMPILED_RULES_CACHE_BASE = "compiled_rules";

    private final DqChecksRepository dqChecksRepository;
    private final CacheVersionRepository cacheVersionRepository;
    private final IgniteCacheService igniteCacheService;

    private Map<Integer, String> changedOrNewRulesSql = new LinkedHashMap<>();
    private Set<Integer> deletedRuleIds = new LinkedHashSet<>();

    @Transactional
    public void refreshCaches() {
        // 1. Читаем всю таблицу
        List<DqChecks> rows = dqChecksRepository.findAll();

        // 2. Строим Map<Integer, String> id -> hash(sqlQuery)
        Map<Integer, String> dbHashes = rows.stream()
                .collect(Collectors.toMap(
                        DqChecks::getId,
                        row -> sha256Hex(row.getSqlQuery()),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        // Дополнительно держим id -> sqlQuery, чтобы потом отдать на компиляцию
        Map<Integer, String> dbSqlById = rows.stream()
                .collect(Collectors.toMap(
                        DqChecks::getId,
                        DqChecks::getSqlQuery,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));

        // 3. Получаем/создаем dqchecks_hash
        ClientCache<Integer, String> hashCache =
                igniteCacheService.getOrCreateCacheByFullName(DQCHECKS_HASH_CACHE);

        Map<Integer, String> cacheHashes =
                igniteCacheService.cacheExistsByFullName(DQCHECKS_HASH_CACHE)
                        ? igniteCacheService.readSnapshotByFullName(DQCHECKS_HASH_CACHE)
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

        for (Integer cachedId : cacheHashes.keySet()) {
            if (!dbHashes.containsKey(cachedId)) {
                this.deletedRuleIds.add(cachedId);
            }
        }

        // 6. Полностью обновляем dqchecks_hash снапшотом
        hashCache.clear();
        if (!dbHashes.isEmpty()) {
            hashCache.putAll(dbHashes);
        }

        RuleCompilerPipeline ruleCompiler = new RuleCompilerPipeline(2);

        Map<Integer, String> normalized = this.changedOrNewRulesSql.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> SqlTextNormalizer.normalize(e.getValue())
                ));

        // 7. Компилируем только новые/изменённые правила
        Map<String, byte[]> compiledDelta =
                normalized.isEmpty()
                        ? Collections.emptyMap()
                        : ruleCompiler.process(normalized);

        // 8. Определяем текущую версию compiled_rules из БД
        CacheVersion currentVersionRow =
                cacheVersionRepository.findTopByIdCacheNameOrderByIdVersionDesc(COMPILED_RULES_CACHE_BASE)
                        .orElse(null);

        Integer currentVersion = currentVersionRow == null
                ? null
                : currentVersionRow.getId().getVersion();
        Integer newVersion = currentVersion == null ? 1 : currentVersion + 1;

        // 9. Собираем новый compiled_rules_<newVersion>
        ClientCache<String, byte[]> newCompiledCache =
                igniteCacheService.getOrCreateVersionedCache(COMPILED_RULES_CACHE_BASE, newVersion);

        // если версия уже есть — очищаем перед заполнением
        newCompiledCache.clear();

        // если есть старая версия, копируем её целиком
        if (currentVersion != null
                && igniteCacheService.cacheExists(COMPILED_RULES_CACHE_BASE, currentVersion)) {

            Map<String, byte[]> previousSnapshot =
                    igniteCacheService.readSnapshot(COMPILED_RULES_CACHE_BASE, currentVersion);

            if (!previousSnapshot.isEmpty()) {
                newCompiledCache.putAll(previousSnapshot);
            }
        }

        ruleCompiler.shutdown();

        // накатываем изменения
        if (!compiledDelta.isEmpty()) {
            newCompiledCache.putAll(compiledDelta);
        }

        // удаляем ключи, которых больше нет в БД
        for (Integer deletedId : this.deletedRuleIds) {
            newCompiledCache.remove(String.format("Rule%s", deletedId));
        }

        // 10. Фиксируем новую версию в БД
        CacheVersion newVersionRow = new CacheVersion(
                new CacheVersionId(COMPILED_RULES_CACHE_BASE, newVersion)
        );
        cacheVersionRepository.save(newVersionRow);
    }

    public Map<Integer, String> getChangedOrNewRulesSql() {
        return Collections.unmodifiableMap(changedOrNewRulesSql);
    }

    public Set<Integer> getDeletedRuleIds() {
        return Collections.unmodifiableSet(deletedRuleIds);
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
