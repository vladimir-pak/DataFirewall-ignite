package com.gpb.datafirewall.controller;
import com.gpb.datafirewall.dto.CompiledRulesResponse;
import com.gpb.datafirewall.service.IgniteCacheService;
import lombok.RequiredArgsConstructor;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.ScanQuery;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class IgniteCacheController {

    private final IgniteCacheService igniteService;

    /**
     * Возвращает все классы из compiled cache:
     * key = "Rule123"
     * value = base64(bytecode)
     */
    @GetMapping("/compiled-rules")
    public CompiledRulesResponse getAll(@RequestParam String sourceName) {
        IgniteCache<String, byte[]> compiledCache = igniteService.getOrCreateCompiledCache(sourceName);

        Set<String> keys = ConcurrentHashMap.newKeySet();
        compiledCache.query(new ScanQuery<String, byte[]>())
                .forEach(e -> keys.add(e.getKey()));

        Map<String, byte[]> raw = compiledCache.getAll(keys);

        var enc = Base64.getEncoder();
        Map<String, String> out = new java.util.LinkedHashMap<>();
        raw.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    if (e.getKey() != null && e.getValue() != null) {
                        out.put(e.getKey(), enc.encodeToString(e.getValue()));
                    }
                });

        String cacheName = String.format("compiled_%s", sourceName);
        return new CompiledRulesResponse(cacheName, sourceName, out.size(), out);
    }
}

