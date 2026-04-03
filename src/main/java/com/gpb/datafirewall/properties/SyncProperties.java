package com.gpb.datafirewall.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "sync")
public class SyncProperties {
    /**
     * Включено ли автоматическое обновление по расписанию
     */
    private boolean enabled = true;

    /**
     * Интервал запуска scheduler в миллисекундах
     */
    private long interval = 60_000L;

    /**
     * Таймаут одного полного обновления в миллисекундах
     */
    private long timeoutMs = 300_000L;
}
