package com.gpb.datafirewall.controller;
import com.gpb.datafirewall.dto.CacheResponseDto;
import com.gpb.datafirewall.dto.RefreshResult;
import com.gpb.datafirewall.model.CacheVersion;
import com.gpb.datafirewall.repository.CacheVersionRepository;
import com.gpb.datafirewall.service.CacheRefreshService;
import com.gpb.datafirewall.service.IgniteCacheService;
import lombok.RequiredArgsConstructor;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.client.ClientCache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import javax.cache.Cache;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class IgniteCacheController {

    private final IgniteCacheService igniteCacheService;
    private final CacheVersionRepository cacheVersionRepository;
    private final CacheRefreshService cacheRefreshService;

    /**
     * Метод для получения кэша по наименованию с версией
     * @param fullCacheName - Полное наименование кэша с версией
     * @return JSON с кэшем. byte[] переводится в String
     */
    @GetMapping("/cache/{fullCacheName}")
    public CacheResponseDto<String, Object> getVersionedCache(@PathVariable String fullCacheName) {
        return getCache(fullCacheName);
    }

    /**
     * Метод для получения кэша с последней версией
     * @param cacheName - наименование кэша без версии
     * @return JSON с кэшем последней версии
     */
    @GetMapping("/cache/latest/{cacheName}")
    public CacheResponseDto<String, Object> getActualCache(@PathVariable String cacheName) {
        CacheVersion currentVersionRow =
                cacheVersionRepository.findTopByIdCacheNameOrderByIdVersionDesc(cacheName)
                        .orElse(null);

        String fullCacheName = String.format("%s_%s", cacheName, currentVersionRow.getId().getVersion());

        return getCache(fullCacheName);
    }

    /**
     * Метод для обновления всего кэша вручную.
     * @return 200 - если отработан. 409 - если уже работает обновление. 408 - timeout. 500 - ошибка обновления
     */
    @PutMapping("/cache/refresh")
    public ResponseEntity<RefreshResult> refreshNow() {
        RefreshResult result = cacheRefreshService.refreshCacheNow();

        if (result.alreadyRunning()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }

        if (result.timeout()) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(result);
        }

        if (!result.success()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Метод для проверки расписания. true - включено, false - выключено.
     * @return boolean
     */
    @GetMapping("/cache/scheduled/status")
    public ResponseEntity<Boolean> isScheduledRefreshEnabled() {
        return ResponseEntity.ok(cacheRefreshService.isScheduledRefreshEnabled());
    }

    /**
     * Метод для включения обновления по расписанию
     * @return String
     */
    @PutMapping("/cache/scheduled/enable")
    public ResponseEntity<String> enableScheduler() {
        cacheRefreshService.enableScheduledRefresh();
        return ResponseEntity.ok("Обновление по расписанию включено");
    }

    /**
     * Метод для выключения обновления по расписанию
     * @return String
     */
    @PutMapping("/cache/scheduled/disable")
    public ResponseEntity<String> disableScheduler() {
        cacheRefreshService.disableScheduledRefresh();
        return ResponseEntity.ok("Обновление по расписанию выключено");
    }

    /**
     * Метод для удаления кэша всех версий
     * @param cacheName
     * @return
     */
    @DeleteMapping("/cache/delete/{cacheName}")
    public ResponseEntity<String> deleteCache(@PathVariable String cacheName) {
        igniteCacheService.destroyAllVersions(cacheName);
        return ResponseEntity.ok("Кэш обновлен");
    }

    


    /**
     * Метод для получения и сериализации кэша
     * @param fullCacheName - Полное наименование кэша с версией
     * @return CacheResponseDto
     */
    private CacheResponseDto<String, Object> getCache(String fullCacheName) {
        ClientCache<String, Object> compiledCache =
            igniteCacheService.getCacheByFullName(fullCacheName);

        Map<String, Object> cacheMap = toMap(compiledCache);

        if (cacheMap == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Кэш с именем '" + fullCacheName + "' не найден или пустой."
            );
        }

        if (fullCacheName.startsWith("compiled_rules")) {
            var enc = Base64.getEncoder();
            Map<String, Object> out = new java.util.LinkedHashMap<>();
            cacheMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(e -> {
                        if (e.getKey() != null && e.getValue() instanceof byte[] bytes) {
                            out.put(e.getKey(), enc.encodeToString(bytes));
                        }
                    });
            return new CacheResponseDto<String, Object>(
                    fullCacheName,
                    out.size(),
                    out
            );
        }

        return new CacheResponseDto<String, Object>(
                fullCacheName,
                cacheMap.size(),
                cacheMap
        );
    }

    private Map<String, Object> toMap(ClientCache<String, Object> cache) {
        Map<String, Object> result = new LinkedHashMap<>();

        try (QueryCursor<Cache.Entry<String, Object>> cursor =
                    cache.query(new ScanQuery<>())) {

            for (Cache.Entry<String, Object> entry : cursor) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
}

