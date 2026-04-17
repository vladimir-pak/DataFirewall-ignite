package com.gpb.datafirewall.service;

public interface KafkaProducerService {
    void send(String cacheName, Integer version);
}
