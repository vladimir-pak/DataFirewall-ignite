package com.gpb.datafirewall.service.impl;

import org.springframework.stereotype.Service;

import com.gpb.datafirewall.service.KafkaProducerService;
import com.gpb.datafirewall.service.ToggleHandlerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ToggleHandlerServiceImpl implements ToggleHandlerService {
    private final KafkaProducerService kafkaProducerService;

    private final static String CACHE_NAME = "handler";
    
    @Override
    public void toggle(String handler) {
        kafkaProducerService.send(
                CACHE_NAME,
                -1
        );
    }
}
