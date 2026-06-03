package com.gpb.datafirewall.service.impl;

import org.apache.ignite.client.ClientCache;
import org.springframework.stereotype.Service;

import com.gpb.datafirewall.service.IgniteCacheService;
import com.gpb.datafirewall.service.KafkaProducerService;
import com.gpb.datafirewall.service.ToggleHandlerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToggleHandlerServiceImpl implements ToggleHandlerService {
    private final IgniteCacheService igniteCacheService; 
    private final KafkaProducerService kafkaProducerService;

    private final static String CACHE_NAME = "handler";
    
    @Override
    public void toggle(String handler) {
        ClientCache<String, String> cacheHandler =
                igniteCacheService.getOrCreateCacheByFullName(CACHE_NAME);

        // если версия уже есть — очищаем перед заполнением
        cacheHandler.put(CACHE_NAME, handler);

        kafkaProducerService.send(
                CACHE_NAME,
                -1
        );
    }

    @Override
    public String getCurrentHandler() {
        ClientCache<String, String> cacheHandler =
                igniteCacheService.getOrCreateCacheByFullName(CACHE_NAME);

        return cacheHandler.get(CACHE_NAME);
    }
}
