package com.gpb.datafirewall.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.gpb.datafirewall.service.SqlParserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StartupWarmupConfig {

    private final SqlParserService sqlParserService;

    @Bean
    public ApplicationRunner warmupCompiledRules() {
        return args -> {
            String sourceName = "УСЛиК";
            log.info("Warmup: parseAll({})", sourceName);
            sqlParserService.parseAll(sourceName);
            log.info("Warmup done");
        };
    }
}

