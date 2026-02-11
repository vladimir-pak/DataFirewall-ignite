package com.gpb.datafirewall.dto;

import java.util.Map;

public record CompiledRulesResponse(
        String cacheName,
        String sourceName,
        int count,
        Map<String, String> classesBase64
) {}

