package com.gpb.datafirewall.service;

import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.query.Query;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.ClientCacheConfiguration;
import org.apache.ignite.client.IgniteClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import javax.cache.Cache;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IgniteCacheService {

    private final IgniteClient igniteClient;

    public <K, V> ClientCache<K, V> getOrCreateCacheByFullName(String fullCacheName) {
        validateFullCacheName(fullCacheName);

        ClientCacheConfiguration cfg = new ClientCacheConfiguration()
                .setName(fullCacheName)
                .setCacheMode(CacheMode.REPLICATED)
                .setAtomicityMode(CacheAtomicityMode.ATOMIC)
                .setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);

        return igniteClient.getOrCreateCache(cfg);
    }

    public <K, V> ClientCache<K, V> getCacheByFullName(String fullCacheName) {
        validateFullCacheName(fullCacheName);
        return igniteClient.cache(fullCacheName);
    }

    public <K, V> ClientCache<K, V> getOrCreateVersionedCache(String baseName, Integer version) {
        String fullCacheName = buildVersionedCacheName(baseName, version);

        ClientCacheConfiguration cfg = new ClientCacheConfiguration()
                .setName(fullCacheName)
                .setCacheMode(CacheMode.REPLICATED)
                .setAtomicityMode(CacheAtomicityMode.ATOMIC)
                .setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);

        return igniteClient.getOrCreateCache(cfg);
    }

    public <K, V> ClientCache<K, V> getVersionedCache(String baseName, Integer version) {
        return this.<K, V>getCacheByFullName(buildVersionedCacheName(baseName, version));
    }

    public <K, V> void putSnapshot(String baseName, Integer version, Map<K, V> snapshot) {
        Objects.requireNonNull(snapshot, "snapshot must not be null");
        ClientCache<K, V> cache = getOrCreateVersionedCache(baseName, version);
        cache.putAll(snapshot);
    }

    public <K, V> Map<K, V> readSnapshot(String baseName, Integer version) {
        ClientCache<K, V> cache = this.<K, V>getVersionedCache(baseName, version);
        return readAll(cache);
    }

    public <K, V> Map<K, V> readSnapshotByFullName(String fullCacheName) {
        ClientCache<K, V> cache = getCacheByFullName(fullCacheName);
        return readAll(cache);
    }

    public <K, V> V getEntry(String baseName, Integer version, K key) {
        return this.<K, V>getVersionedCache(baseName, version).get(key);
    }

    public <K, V> void putEntry(String baseName, Integer version, K key, V value) {
        ClientCache<K, V> cache = this.<K, V>getOrCreateVersionedCache(baseName, version);
        cache.put(key, value);
    }

    public void destroyCacheByFullName(String fullCacheName) {
        validateFullCacheName(fullCacheName);
        igniteClient.destroyCache(fullCacheName);
    }

    public void destroyVersionedCache(String baseName, Integer version) {
        igniteClient.destroyCache(buildVersionedCacheName(baseName, version));
    }

    public void clearCacheByFullName(String fullCacheName) {
        getCacheByFullName(fullCacheName).clear();
    }

    public int destroyAllVersions(String baseName) {
        String prefix = normalizeBaseName(baseName) + "_";
        int deleted = 0;

        for (String cacheName : igniteClient.cacheNames()) {
            if (cacheName.startsWith(prefix)) {
                igniteClient.destroyCache(cacheName);
                deleted++;
            }
        }

        return deleted;
    }

    public boolean cacheExistsByFullName(String fullCacheName) {
        validateFullCacheName(fullCacheName);
        return igniteClient.cacheNames().contains(fullCacheName);
    }

    public boolean cacheExists(String baseName, Integer version) {
        return igniteClient.cacheNames().contains(buildVersionedCacheName(baseName, version));
    }

    public String buildVersionedCacheName(String baseName, Integer version) {
        validateBaseName(baseName);
        validateVersion(version);
        return normalizeBaseName(baseName) + "_" + version;
    }

    private <K, V> Map<K, V> readAll(ClientCache<K, V> cache) {
        Map<K, V> result = new LinkedHashMap<>();

        Query<Cache.Entry<K, V>> query = new ScanQuery<>();
        try (QueryCursor<Cache.Entry<K, V>> cursor = cache.query(query)) {
            for (Cache.Entry<K, V> entry : cursor) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    private void validateBaseName(String baseName) {
        if (baseName == null || baseName.isBlank()) {
            throw new IllegalArgumentException("baseName must not be blank");
        }
    }

    private String normalizeBaseName(String baseName) {
        return baseName.trim();
    }

    private void validateVersion(Integer version) {
        if (version == null || version < 0) {
            throw new IllegalArgumentException("version must not be null or negative");
        }
    }

    private void validateFullCacheName(String fullCacheName) {
        if (fullCacheName == null || fullCacheName.isBlank()) {
            throw new IllegalArgumentException("fullCacheName must not be blank");
        }
    }
}
