package com.gpb.datafirewall.service.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.gpb.datafirewall.service.KafkaProducerService;

/**
 * Класс - заглушка при выключенном использовании Kafka
 */
@Service
@ConditionalOnProperty(
        prefix = "app.kafka.cache-update",
        name = "enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class NoopKafkaProducerServiceImpl implements KafkaProducerService {

    @Override
    public void send(String cacheName, Integer version) {
        // Kafka disabled, do nothing
        System.out.println("Kafka disabled");
    }
}
