package com.gpb.datafirewall.service.impl;

import com.gpb.datafirewall.service.IgniteCacheService;
import com.gpb.datafirewall.service.KafkaProducerService;

import lombok.RequiredArgsConstructor;

import com.gpb.datafirewall.model.CacheVersion;
import com.gpb.datafirewall.model.CacheVersionId;
import com.gpb.datafirewall.model.PgStat;
import com.gpb.datafirewall.properties.Caches;
import com.gpb.datafirewall.repository.CacheVersionRepository;
import com.gpb.datafirewall.repository.PgStatRepository;
import com.gpb.datafirewall.repository.PoliticsRepository;

import org.apache.ignite.client.ClientCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PoliticsCacheRefreshServiceImpl {

    private final PgStatRepository pgStatRepository;
    private final CacheVersionRepository cacheVersionRepository;
    private final IgniteCacheService igniteCacheService;
    private final PoliticsRepository politicsRepository;
    private final KafkaProducerService kafkaProducerService;

    private static final String DB_CACHE_NAME = "politics";
    private static final List<String> SCANNING_TABLES = 
            List.of("dqchecks", "dictionary_items", "dataset2control_area", "dataset_exclusion");
    private static final String SCANNING_SCHEMA = "datafirewall";

    @Transactional
    public void refreshCaches() {
        // 1. Читаем всю таблицу
        List<PgStat> rows = pgStatRepository.findTableStats(SCANNING_TABLES, SCANNING_SCHEMA);

        // 2. Получаем/создаем dqchecks_hash
        Map<String, Long> statMap =
                igniteCacheService.cacheExistsByFullName(Caches.PG_STAT.getName())
                        ? igniteCacheService.readSnapshotByFullName(Caches.PG_STAT.getName())
                        : new LinkedHashMap<>();

        Map<String, Long> newStatCache = new HashMap<>();

        // 3. Сравниваем
        boolean isChanged = false;
        for (PgStat row : rows) {
            newStatCache.put(row.tableName(), row.totalChanges());

            Long cacheChanges = statMap.get(row.tableName());
            if (!Objects.equals(row.totalChanges(), cacheChanges)) {
                isChanged = true;
            }
        }

        if (statMap.size() != newStatCache.size()) {
            isChanged = true;
        }

        // 4. Обновляем кэши, если есть изменения
        if (isChanged) {
            // 4.1 Получаем новую версию
            CacheVersion currentVersionRow =
                    cacheVersionRepository.findTopByIdCacheNameOrderByIdVersionDesc(DB_CACHE_NAME)
                            .orElse(null);

            Integer currentVersion = currentVersionRow == null
                    ? null
                    : currentVersionRow.getId().getVersion();
            Integer newVersion = currentVersion == null ? 1 : currentVersion + 1;

            // 4.2 Обновляем кэши
            try {
                Map<String, String> dataset2ControlArea = politicsRepository.getDataset2ControlArea();
                createNewVersion(Caches.POLITICS_DATASET2CONTROL_AREA.getName(), newVersion, dataset2ControlArea);
                
                Map<Integer, String> errorMessages = politicsRepository.getErrorMessages();
                createNewVersion(Caches.POLITICS_ERROR_MESSAGES.getName(), newVersion, errorMessages);

                Map<String, Set<String>> datasetExclusion = politicsRepository.getDatasetExclusion();
                createNewVersion(Caches.POLITICS_DATASET_EXCLUSION.getName(), newVersion, datasetExclusion);

                Map<String, Map<String, Set<Integer>>> controlAreaRules = politicsRepository.getControlAreaRules();
                createNewVersion(Caches.POLITICS_CONTROL_AREA_RULES.getName(), newVersion, controlAreaRules);

                ClientCache<String, Long> statCache =
                        igniteCacheService.getOrCreateCacheByFullName(Caches.PG_STAT.getName());
                statCache.putAll(newStatCache);

                // 5. Фиксируем новую версию в БД
                CacheVersion newVersionRow = new CacheVersion(
                        new CacheVersionId(DB_CACHE_NAME, newVersion)
                );
                cacheVersionRepository.save(newVersionRow);
            } catch (Exception ex) {
                igniteCacheService.destroyVersionedCache(Caches.POLITICS_DATASET2CONTROL_AREA.getName(), newVersion);
                igniteCacheService.destroyVersionedCache(Caches.POLITICS_ERROR_MESSAGES.getName(), newVersion);
                igniteCacheService.destroyVersionedCache(Caches.POLITICS_DATASET_EXCLUSION.getName(), newVersion);
                igniteCacheService.destroyVersionedCache(Caches.POLITICS_CONTROL_AREA_RULES.getName(), newVersion);
                throw new RuntimeException("Ошибка при обновлении кэша politics: " + ex);
            }

            // 6. Удаляем старые кэши, за исключением нового и предыдущего
            if (currentVersion != null) {
                destroyOldCaches(Caches.POLITICS_DATASET2CONTROL_AREA.getName(), currentVersion, newVersion);
                destroyOldCaches(Caches.POLITICS_ERROR_MESSAGES.getName(), currentVersion, newVersion);
                destroyOldCaches(Caches.POLITICS_DATASET_EXCLUSION.getName(), currentVersion, newVersion);
                destroyOldCaches(Caches.POLITICS_CONTROL_AREA_RULES.getName(), currentVersion, newVersion);
            }

            // 7. Отправляем событие об обновлении в Kafka
            kafkaProducerService.send(
                    Caches.COMPILED_RULES.getName(),
                    newVersion
            );
        }
    }

    private <K, V> void createNewVersion(String cacheName, Integer newVersion, Map<K, V> newData) {
        ClientCache<K, V> newCompiledCache =
                igniteCacheService.getOrCreateVersionedCache(cacheName, newVersion);

        // если версия уже есть — очищаем перед заполнением
        newCompiledCache.clear();
        newCompiledCache.putAll(newData);
    }

    private void destroyOldCaches(String cacheName, int currentVersion, int newVersion) {
        String currentFullCacheName = String.format("%s_%s", cacheName, currentVersion);
        String newFullCacheName = String.format("%s_%s", cacheName, newVersion);
        String prefix = cacheName + "_";

        for (String existingCacheName : igniteCacheService.getCacheNames()) {
            if (!existingCacheName.startsWith(prefix)) {
                continue;
            }

            String suffix = existingCacheName.substring(prefix.length());
            if (!suffix.matches("\\d+")) {
                continue;
            }

            if (!existingCacheName.equals(currentFullCacheName) && !existingCacheName.equals(newFullCacheName)) {
                igniteCacheService.destroyCacheByFullName(existingCacheName);
            }
        }
    }
}
