package com.gpb.datafirewall.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.gpb.datafirewall.model.SqlExpression;
import com.gpb.datafirewall.repository.SqlExpressionRepository;
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
    private final SqlExpressionRepository repository;

    @Override
    public void parse(String sourceName) {
        IgniteCache<String, byte[]> compiledCache = igniteService.getOrCreateCompiledCache(sourceName);
        CacheComparisonResult changes = igniteService.compareCaches(sourceName);

               // Удаление из кэша удаленных проверок
        Set<String> keysToDelete = changes.getDeletedRecords().keySet().stream()
                .map(num -> "Rule" + num)
                .collect(Collectors.toSet());
        compiledCache.removeAll(keysToDelete);

        RuleCompilerPipeline pipeline = new RuleCompilerPipeline(2);

        Map<Integer, String> rules = changes.getNewRecords();
        if (!changes.getModifiedRecords().isEmpty()) {
            rules.putAll(changes.getModifiedRecords());
        }

        Map<String, byte[]> compiled = pipeline.process(rules);
        log.info("Compiled {} rules", compiled.size());

        pipeline.shutdown();

        compiledCache.putAll(compiled);
    }

    /**
     * ПОЛНАЯ пересборка: компилирует все правила из БД и кладёт в compiledCache
     * (для первого запуска /   ручная- ресинхронизации)
     */
    @Override
    public void parseAll(String sourceName) {
        IgniteCache<String, byte[]> compiledCache = igniteService.getOrCreateCompiledCache(sourceName);

        Map<Integer, String> rulesSql = repository.findBySourceName(sourceName).stream()
                .collect(Collectors.toMap(SqlExpression::getId, SqlExpression::getSql));

        if (rulesSql.isEmpty()) {
            log.warn("No sql_expressions found in DB for sourceName={}", sourceName);
            return;
        }

        RuleCompilerPipeline pipeline = new RuleCompilerPipeline(2);
        Map<String, byte[]> compiled = pipeline.process(rulesSql);
        pipeline.shutdown();

        log.info("Compiled {} rules (FULL) for sourceName={}", compiled.size(), sourceName);

        // чтобы не оставались старые классы
        compiledCache.clear();
        compiledCache.putAll(compiled);
    }

}
