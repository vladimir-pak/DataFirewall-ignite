package com.gpb.datafirewall.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gpb.datafirewall.service.FlinkClassLoaderService;
import com.gpb.datafirewall.service.TestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test")
public class TestController {
    private final TestService testService;
    private final FlinkClassLoaderService flinkClassLoaderService;

    @PostMapping("/start")
    public ResponseEntity<String> start(@RequestBody Map<String, String> testData) {
        testService.test(testData);
        return ResponseEntity.ok("Test finished");
    }

    @PostMapping("/compile/{serviceName}")
    public ResponseEntity<String> compile(@PathVariable String serviceName) {
        flinkClassLoaderService.updateRules(serviceName);
        return ResponseEntity.ok("Compiled");
    }
}
