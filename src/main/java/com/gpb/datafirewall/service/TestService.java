package com.gpb.datafirewall.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.gpb.datafirewall.model.Rule;
import com.gpb.datafirewall.utils.CompiledRulesContainer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Сервис для тестирования через API
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {
    private final CompiledRulesContainer container;

    public void test(Map<String, String> testData) {
        Map<String, Rule> rules = container.snapshot();

        rules.entrySet()
                .parallelStream()
                .forEach(e -> {
                    boolean result = e.getValue().apply(testData);
                    System.out.println(e.getKey() + " => " + result);
                });
    }
}
