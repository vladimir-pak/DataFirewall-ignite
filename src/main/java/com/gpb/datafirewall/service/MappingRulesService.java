package com.gpb.datafirewall.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.ignite.IgniteCache;
import org.springframework.stereotype.Service;

import com.gpb.datafirewall.dto.RuleList;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MappingRulesService {
    private final IgniteCacheService igniteCacheService;

    private Map<String, List<Integer>> getRulesFromDb(String sourceName) {
        // TODO: бизнес-логика отбора проверок для системы
        return Collections.emptyMap();
    }

    public void setRulesBySource(String sourceName) {
        String cacheName = String.format("rules_%s", sourceName);
        IgniteCache<String, RuleList> cache = igniteCacheService.getOrCreateCustomCache(
            cacheName, String.class, RuleList.class);
        

    }

    public IgniteCache<String, RuleList> getRulesBySource(String sourceName) {
        String cacheName = String.format("rules_%s", sourceName);
        return igniteCacheService.getOrCreateCustomCache(
            cacheName, String.class, RuleList.class);
    }
}
