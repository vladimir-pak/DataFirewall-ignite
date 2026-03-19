package com.gpb.datafirewall.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ScanQuery;
import org.springframework.stereotype.Service;

import com.gpb.datafirewall.compile.CompiledRulesContainer;
import com.gpb.datafirewall.compile.RuleClassLoader;
import com.gpb.datafirewall.model.Rule;
import com.gpb.datafirewall.service.impl.IgniteCacheServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Сервис для загрузки Java классов из byte[] в Map (runtime)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlinkClassLoaderService {

    private final IgniteCacheServiceImpl igniteService;
    private final CompiledRulesContainer compiledRules;
    private final SqlParserService sqlParserService;

    public void updateRules(String sourceName) {
        log.info("Starting parsing sql rules...");
        sqlParserService.parseAll(sourceName);

        reloadAllRulesFromCache(sourceName);
    }

    private void reloadAllRulesFromCache(String sourceName) {
        IgniteCache<String, byte[]> compiledCache = igniteService.getOrCreateCompiledCache(sourceName);

        Set<String> keys = ConcurrentHashMap.newKeySet();
        compiledCache.query(new ScanQuery<String, byte[]>())
                .forEach(entry -> keys.add(entry.getKey()));

        Map<String, byte[]> mapCache = compiledCache.getAll(keys);

        RuleClassLoader classLoader = new RuleClassLoader(mapCache);

        Map<String, Rule> newRules = new HashMap<>();

        List<String> sorted = keys.stream().sorted().toList();
        for (String name : sorted) {
            try {
                Class<? extends Rule> ruleClass = classLoader.loadRule(name);
                Rule ruleInstance = ruleClass.getDeclaredConstructor().newInstance();
                newRules.put(name, ruleInstance);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load class " + name, e);
            }
        }

        compiledRules.replaceAll(newRules);
    }
}
