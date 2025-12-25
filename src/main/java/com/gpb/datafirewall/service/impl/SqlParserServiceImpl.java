package com.gpb.datafirewall.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.ignite.IgniteCache;
import org.springframework.stereotype.Service;

import com.gpb.datafirewall.cache.CacheComparisonResult;
import com.gpb.datafirewall.cef.SvoiLogger;
import com.gpb.datafirewall.compile.RuleCompilerPipeline;
import com.gpb.datafirewall.service.SqlParserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Парсинг SQL -> AST -> byte[] и запись в IgniteCache.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SqlParserServiceImpl implements SqlParserService {
    private final IgniteCacheServiceImpl igniteService;
    private final SvoiLogger logger;

    @Override
    public void parse(String sourceName) {
        IgniteCache<String, byte[]> compiledCache = igniteService.getOrCreateCompiledCache(sourceName);
        CacheComparisonResult changes = igniteService.compareCaches(sourceName);

        // Удаление из кэша удаленных проверок
        Set<String> keysToDelete = changes.getDeletedRecords().keySet().stream()
                .map(num -> "Rule" + num)
                .collect(Collectors.toSet());
        compiledCache.removeAll(keysToDelete);

        // Добавление новых проверок и измененных
        // Выполняется через RulePipeline - параллельная компиляция
        RuleCompilerPipeline pipeline = new RuleCompilerPipeline(
            2 // количество параллельных потоков
        );

        Map<Integer, String> rules = changes.getNewRecords();
        if (!changes.getModifiedRecords().isEmpty()) {
            rules.putAll(changes.getModifiedRecords());
        }
        
        Map<String, byte[]> compiled = pipeline.process(rules);
        log.info("Compiled {} rules", compiled.size());

        pipeline.shutdown();

        compiledCache.putAll(compiled);
    }
}
