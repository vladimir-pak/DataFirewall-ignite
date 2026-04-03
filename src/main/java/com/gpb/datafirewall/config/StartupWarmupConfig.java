package com.gpb.datafirewall.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gpb.datafirewall.service.CacheRefreshService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StartupWarmupConfig {

    private final CacheRefreshService cacheRefreshService;

    @Bean
    public ApplicationRunner warmupCompiledRules() {
        return args -> {
            log.info("Warmup: preparing cache...");
            cacheRefreshService.refreshCache();
            log.info("Warmup done");
        };
    }
}

