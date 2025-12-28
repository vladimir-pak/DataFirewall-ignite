package com.gpb.datafirewall.service;

import org.apache.ignite.IgniteCache;

import com.gpb.datafirewall.cache.CacheComparisonResult;

public interface IgniteCacheService {
    public IgniteCache<String, byte[]> getOrCreateCompiledCache(String sourceName); 
    public <K, V> IgniteCache<K, V> getOrCreateCustomCache(
            String cacheName,
            Class<K> keyType,
            Class<V> valType
    );
    public CacheComparisonResult synchronizeWithDatabase(String sourceName);
}