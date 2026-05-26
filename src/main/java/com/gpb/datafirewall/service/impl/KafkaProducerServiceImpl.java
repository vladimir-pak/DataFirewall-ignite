package com.gpb.datafirewall.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.gpb.datafirewall.cef.SvoiLogger;
import com.gpb.datafirewall.cef.enums.SvoiSeverityEnum;
import com.gpb.datafirewall.dto.CacheUpdateMessage;
import com.gpb.datafirewall.service.KafkaProducerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "spring.kafka.cache-update",
        name = "enabled",
        havingValue = "true"
)
public class KafkaProducerServiceImpl implements KafkaProducerService {
    private final SvoiLogger svoiCustomLogger;
    private final KafkaTemplate<String, CacheUpdateMessage> kafkaTemplate;

    @Value("${spring.kafka.topics.cache-update}")
    private String topic;

    public void send(String cacheName, Integer version) {
        CacheUpdateMessage message = new CacheUpdateMessage(cacheName, version);
        svoiCustomLogger.sendInternal(
            "sendKafkaMessage", 
            "Send Kafka Message", 
            String.format("Updated Cache: %s version %s", cacheName, version),
            SvoiSeverityEnum.ONE
        );
        kafkaTemplate.send(topic, cacheName, message);
    }
}
