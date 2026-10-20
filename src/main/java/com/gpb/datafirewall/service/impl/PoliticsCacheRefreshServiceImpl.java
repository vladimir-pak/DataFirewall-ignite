package com.gpb.datafirewall.service.impl;

import com.gpb.datafirewall.service.CacheVersionPublisher;
import com.gpb.datafirewall.service.IgniteCacheService;
import com.gpb.datafirewall.service.KafkaProducerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.gpb.datafirewall.model.CacheVersion;
import com.gpb.datafirewall.model.PgStat;
import com.gpb.datafirewall.properties.Caches;
import com.gpb.datafirewall.repository.CacheVersionRepository;
import com.gpb.datafirewall.repository.PgStatRepository;
import com.gpb.datafirewall.repository.PoliticsRepository;

import org.apache.ignite.client.ClientCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PoliticsCacheRefreshServiceImpl {

    private final PgStatRepository pgStatRepository;
    private final CacheVersionRepository cacheVersionRepository;
    private final IgniteCacheService igniteCacheService;
    private final PoliticsRepository politicsRepository;
    private final KafkaProducerService kafkaProducerService;

    private final CacheVersionPublisher cacheVersionPublisher;

    private static final String DB_CACHE_NAME = "politics";
    private static final List<String> SCANNING_TABLES = 
            List.of(
                    "dqchecks", 
                    "dictionary_items", 
                    "dataset2control_area",
                    "dataset_exclusion",
                    "control_areas_in_check"
            );
    private static final String SCANNING_SCHEMA = "datafirewall";   

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public synchronized void refreshCaches() {
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

            // 1. Полностью подготавливаем новую версию в Ignite.
            // До commit в БД она не считается опубликованной.

            createNewVersion(
                    Caches.POLITICS_DATASET2CONTROL_AREA.getName(),
                    newVersion,
                    politicsRepository.getDataset2ControlArea()
            );

            createNewVersion(
                    Caches.POLITICS_ERROR_MESSAGES.getName(),
                    newVersion,
                    politicsRepository.getErrorMessages()
            );

            createNewVersion(
                    Caches.POLITICS_DATASET_EXCLUSION.getName(),
                    newVersion,
                    politicsRepository.getDatasetExclusion()
            );

            createNewVersion(
                    Caches.POLITICS_CONTROL_AREA_RULES.getName(),
                    newVersion,
                    politicsRepository.getControlAreaRules()
            );

            createNewVersion(
                    Caches.POLITICS_FILTER_FLAG.getName(),
                    newVersion,
                    politicsRepository.getFilterFlags()
            );

            createNewVersion(
                    Caches.GENDER_MAPPING.getName(),
                    newVersion,
                    politicsRepository.getGenderMapping()
            );

            createNewVersion(
                    Caches.TYPE_DUL_MAPPING.getName(),
                    newVersion,
                    politicsRepository.getTypeDulMapping()
            );

            // 2. Публикуем версию в отдельной транзакции БД.
            //
            // При исключении выполнение прервётся до обновления pg_stat.
            // Кэши здесь не удаляем: при потере соединения во время commit
            // результат транзакции может быть неизвестен клиенту.

            cacheVersionPublisher.publish(DB_CACHE_NAME, newVersion);

            // Здесь commit БД уже успешно завершён.

            // 3. Полностью заменяем снимок статистики.
            //
            // Ошибка этого шага не должна удалять опубликованные кэши
            // или препятствовать попытке отправить уведомление.

            try {
                ClientCache<String, Long> statCache =
                        igniteCacheService.getOrCreateCacheByFullName(
                                Caches.PG_STAT.getName()
                        );

                statCache.clear();

                if (!newStatCache.isEmpty()) {
                    statCache.putAll(newStatCache);
                }
            } catch (RuntimeException ex) {
                log.error(
                        "Версия politics {} опубликована, но pg_stat не сохранён. "
                                + "Следующий запуск может повторить обновление",
                        newVersion,
                        ex
                );
            }

            // 4. Уведомление отправляется только после успешного commit БД.

            kafkaProducerService.send(DB_CACHE_NAME, newVersion);

            // 5. Очищаем старые версии.
            // Ошибка очистки не отменяет уже опубликованную версию.

            if (currentVersion != null) {
                List<Caches> politicsCaches = List.of(
                        Caches.POLITICS_DATASET2CONTROL_AREA,
                        Caches.POLITICS_ERROR_MESSAGES,
                        Caches.POLITICS_DATASET_EXCLUSION,
                        Caches.POLITICS_CONTROL_AREA_RULES,
                        Caches.POLITICS_FILTER_FLAG,
                        Caches.GENDER_MAPPING,
                        Caches.TYPE_DUL_MAPPING
                );

                for (Caches cache : politicsCaches) {
                    try {
                        destroyOldCaches(
                                cache.getName(),
                                currentVersion,
                                newVersion
                        );
                    } catch (RuntimeException ex) {
                        log.warn(
                                "Версия politics {} опубликована, "
                                        + "но очистка старых версий {} не выполнена",
                                newVersion,
                                cache.getName(),
                                ex
                        );
                    }
                }
            }            
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
