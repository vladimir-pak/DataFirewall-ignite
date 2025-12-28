package com.gpb.datafirewall.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gpb.datafirewall.cache.CacheComparisonResult;
import com.gpb.datafirewall.model.SqlExpression;
import com.gpb.datafirewall.repository.SqlExpressionRepository;
import com.gpb.datafirewall.service.IgniteCacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Сервис для работы с Ignite Cache
 * compiledRulesCache - кэш для хранения byte[]. Ключ - Наименование класса, например Rule123
 * dbCache - кэш с последним обработанным snapshot всех проверок из базы
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IgniteCacheServiceImpl implements IgniteCacheService {

    private final SqlExpressionRepository repository;

    @Qualifier("igniteInstance")
    protected final Ignite ignite;

    protected final Map<String, IgniteCache<String, byte[]>> compiledRulesCache = new ConcurrentHashMap<>();
    protected final Map<String, IgniteCache<Integer, String>> dbCache = new ConcurrentHashMap<>();
    private final Map<String, IgniteCache<?, ?>> customCache = new ConcurrentHashMap<>();

    protected final String COMPILED_RULES_PREFIX = "compiled_%s";
    protected final String DB_CACHE_PREFIX = "db_%s_";
    protected final String TEMP_DB_CACHE_PREFIX = "temp_db_%s_";
    protected final String CUSTOM_CACHE_PREFIX = "cache_%s";

    private <K, V> IgniteCache<K, V> getOrCreateCache(
            String cacheName,
            Class<K> keyType,
            Class<V> valType,
            Consumer<CacheConfiguration<K, V>> configurer
    ) {
        CacheConfiguration<K, V> cacheCfg = new CacheConfiguration<>(cacheName);

        // Общие настройки
        cacheCfg.setReadFromBackup(true);
        cacheCfg.setStatisticsEnabled(false);
        cacheCfg.setCopyOnRead(false);
        cacheCfg.setIndexedTypes(keyType, valType);

        // Специфичные настройки для конкретного кэша
        if (configurer != null) {
            configurer.accept(cacheCfg);
        }

        return ignite.getOrCreateCache(cacheCfg);
    }

    /**
     * Получить или создать runtime кэш с компилированным кодом проверок по sourceName - источник
     */
    @Override
    public IgniteCache<String, byte[]> getOrCreateCompiledCache(String sourceName) {
        return compiledRulesCache.computeIfAbsent(sourceName, key -> {
            String cacheName = String.format(COMPILED_RULES_PREFIX, sourceName);

            return getOrCreateCache(
                    cacheName,
                    String.class,
                    byte[].class,
                    cfg -> {
                        cfg.setCacheMode(CacheMode.REPLICATED);
                        cfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
                    }
            );
        });
    }

    /**
     * 
     * @param <K> Тип данных ключа для кэша
     * @param <V> Тип данных значения по ключу кэша
     * @param cacheName Наименование кэша (String)
     * @param keyType Класс типа данных ключа для формирования кэша
     * @param valType Класс типа данных значения для формирования кэша
     * @return IgniteCache<K, V> - кэш с заданными типами
     */
    @Override
    public <K, V> IgniteCache<K, V> getOrCreateCustomCache(
            String cacheName,
            Class<K> keyType,
            Class<V> valType
    ) {
        String finalCacheName = String.format("cache_%s", cacheName);

        // Map хранит raw-типы, кастуем при получении
        @SuppressWarnings("unchecked")
        IgniteCache<K, V> cache = (IgniteCache<K, V>) customCache.computeIfAbsent(
            finalCacheName,
            key -> getOrCreateCache(
                finalCacheName,
                keyType,
                valType,
                cfg -> {
                    cfg.setCacheMode(CacheMode.REPLICATED);
                    cfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
                }
            )
        );
        return cache;
    }

    /**
     * Получить или создать runtime кэш с sql из базы по sourceName
     */
    protected IgniteCache<Integer, String> getOrCreateDbCache(String sourceName) {
        return dbCache.computeIfAbsent(sourceName, key -> {
            String cacheName = String.format(DB_CACHE_PREFIX, sourceName);

            return getOrCreateCache(
                    cacheName,
                    Integer.class,
                    String.class,
                    cfg -> {
                        cfg.setCacheMode(CacheMode.REPLICATED);
                        // если нужно, можно добавить ещё специфичные настройки
                    }
            );
        });
    }

    /**
     * Создать временный кэш из данных БД
     */
    protected IgniteCache<Integer, String> createTempCacheFromDatabase(String sourceName) {
        String tempCacheName = String.format(TEMP_DB_CACHE_PREFIX, sourceName) +
                "_" + System.currentTimeMillis();

        IgniteCache<Integer, String> tempCache = getOrCreateCache(
                tempCacheName,
                Integer.class,
                String.class,
                cfg -> cfg.setCacheMode(CacheMode.PARTITIONED)
        );

        List<SqlExpression> dbData = repository.findBySourceName(sourceName);
        Map<Integer, String> tempData = dbData.stream()
                .collect(Collectors.toMap(SqlExpression::getId, SqlExpression::getSql));

        tempCache.putAll(tempData);
        return tempCache;
    }

    /**
     * Полная синхронизация проверок с базой: сравнить и обновить
     */
    @Override
    public CacheComparisonResult synchronizeWithDatabase(String sourceName) {
        CacheComparisonResult changes = compareCaches(sourceName);
        updateRuntimeCache(sourceName, changes);
        return changes;
    }

    /**
     * Обновить runtime кэш на основе временного кэша
     */
    protected void updateRuntimeCache(String sourceName, CacheComparisonResult changes) {
        IgniteCache<Integer, String> runtimeCache = getOrCreateDbCache(sourceName);

        if (!changes.getDeletedRecords().isEmpty()) {
            runtimeCache.removeAll(changes.getDeletedRecords().keySet());
        }

        Map<Integer, String> recordsToUpdate = new HashMap<>();
        recordsToUpdate.putAll(changes.getNewRecords());
        recordsToUpdate.putAll(changes.getModifiedRecords());

        if (!recordsToUpdate.isEmpty()) {
            runtimeCache.putAll(recordsToUpdate);
        }
    }

    /**
     * Сравнить runtime кэш с временным (из БД) и найти изменения
     */
    protected CacheComparisonResult compareCaches(String sourceName) {
        IgniteCache<Integer, String> runtimeCache = getOrCreateDbCache(sourceName);
        IgniteCache<Integer, String> tempCache = createTempCacheFromDatabase(sourceName);

        try {
            CacheComparisonResult result = new CacheComparisonResult();

            findNewRecords(runtimeCache, tempCache, result);
            findModifiedRecords(runtimeCache, tempCache, result);
            findDeletedRecords(runtimeCache, tempCache, result);

            log.info("Comparison result for {}: newRecords={}, modifiedRecords={}, deletedRecords={}",
                    sourceName,
                    result.getNewRecords().size(),
                    result.getModifiedRecords().size(),
                    result.getDeletedRecords().size()
            );

            return result;

        } finally {
            tempCache.destroy();
        }
    }

    private void findNewRecords(IgniteCache<Integer, String> runtimeCache,
                                IgniteCache<Integer, String> tempCache,
                                CacheComparisonResult result) {
        Set<Integer> tempKeys = getAllKeys(tempCache);
        Set<Integer> runtimeKeys = getAllKeys(runtimeCache);

        tempKeys.removeAll(runtimeKeys);

        for (Integer key : tempKeys) {
            String tempData = tempCache.get(key);
            if (tempData != null) {
                result.addNewRecord(key, tempData);
            }
        }
    }

    private void findModifiedRecords(IgniteCache<Integer, String> runtimeCache,
                                     IgniteCache<Integer, String> tempCache,
                                     CacheComparisonResult result) {
        Set<Integer> commonKeys = new HashSet<>(getAllKeys(runtimeCache));
        commonKeys.retainAll(getAllKeys(tempCache));

        for (Integer key : commonKeys) {
            String runtimeData = runtimeCache.get(key);
            String tempData = tempCache.get(key);

            if (runtimeData != null && tempData != null &&
                    !runtimeData.equals(tempData)) {
                result.addModifiedRecord(key, tempData);
            }
        }
    }

    private void findDeletedRecords(IgniteCache<Integer, String> runtimeCache,
                                    IgniteCache<Integer, String> tempCache,
                                    CacheComparisonResult result) {
        Set<Integer> runtimeKeys = getAllKeys(runtimeCache);
        Set<Integer> tempKeys = getAllKeys(tempCache);

        runtimeKeys.removeAll(tempKeys);

        for (Integer key : runtimeKeys) {
            String runtimeData = runtimeCache.get(key);
            if (runtimeData != null) {
                result.addDeletedRecord(key, runtimeData);
            }
        }
    }

    private Set<Integer> getAllKeys(IgniteCache<Integer, String> cache) {
        Set<Integer> keys = ConcurrentHashMap.newKeySet();

        cache.query(new ScanQuery<Integer, String>())
                .forEach(entry -> keys.add(entry.getKey()));

        return keys;
    }

    /**
     * Удалить runtime кэш для cacheName
     */
    public void destroyRuntimeCache(String cacheName) {
        IgniteCache<Integer, String> cache = dbCache.remove(cacheName);
        if (cache != null) {
            cache.destroy();
        }
    }

}
