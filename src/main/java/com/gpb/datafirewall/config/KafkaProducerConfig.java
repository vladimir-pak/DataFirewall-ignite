package com.gpb.datafirewall.config;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.gpb.datafirewall.dto.CacheUpdateMessage;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, CacheUpdateMessage> producerFactory(
            KafkaProperties properties
    ) {
        Map<String, Object> config = properties.buildProducerProperties();

        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, CacheUpdateMessage> kafkaTemplate(
            ProducerFactory<String, CacheUpdateMessage> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }
}
