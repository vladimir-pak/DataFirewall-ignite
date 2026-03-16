package com.gpb.datafirewall.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeneratedRuleSource {
    private String className;
    private String source;
    private byte[] compiledBytes;
}