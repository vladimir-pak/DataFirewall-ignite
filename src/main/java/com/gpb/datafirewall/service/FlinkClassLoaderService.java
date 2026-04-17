package com.gpb.datafirewall.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.cache.Cache;

import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.client.ClientCache;
import org.springframework.stereotype.Service;

import com.gpb.datafirewall.model.CacheVersion;
import com.gpb.datafirewall.model.Rule;
import com.gpb.datafirewall.properties.Caches;
import com.gpb.datafirewall.repository.CacheVersionRepository;
import com.gpb.datafirewall.utils.CompiledRulesContainer;
import com.gpb.datafirewall.utils.RuleClassLoader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Сервис для загрузки Java классов из byte[] в Map (runtime)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlinkClassLoaderService {

    private final IgniteCacheService igniteService;
    private final CompiledRulesContainer compiledRules;
    private final CacheVersionRepository cacheVersionRepository;

    public void updateRules() {
        log.info("Запуск получения экземпляров классов проверок...");
        reloadAllRulesFromCache();
    }

    private void reloadAllRulesFromCache() {

        CacheVersion currentVersionRow =
                cacheVersionRepository.findTopByIdCacheNameOrderByIdVersionDesc(Caches.COMPILED_RULES.getName())
                        .orElse(null);

        String fullCacheName = 
                String.format("%s_%s", Caches.COMPILED_RULES.getName(), currentVersionRow.getId().getVersion());
        ClientCache<String, byte[]> compiledCache = igniteService.getCacheByFullName(fullCacheName);

        Map<String, byte[]> cacheMap = toMap(compiledCache);

        RuleClassLoader classLoader = new RuleClassLoader(cacheMap);

        Map<String, Rule> newRules = new HashMap<>();

        cacheMap.entrySet().stream().sorted()
                .forEach(entry -> {
                    try {
                        Class<? extends Rule> ruleClass = classLoader.loadRule(entry.getKey());
                        Rule ruleInstance = ruleClass.getDeclaredConstructor().newInstance();
                        newRules.put(entry.getKey(), ruleInstance);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load class " + entry.getKey(), e);
                    }
                });

        compiledRules.replaceAll(newRules);
    }

    private <V> Map<String, V> toMap(ClientCache<String, V> cache) {
        Map<String, V> result = new LinkedHashMap<>();

        try (QueryCursor<Cache.Entry<String, V>> cursor =
                    cache.query(new ScanQuery<>())) {

            for (Cache.Entry<String, V> entry : cursor) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
}
