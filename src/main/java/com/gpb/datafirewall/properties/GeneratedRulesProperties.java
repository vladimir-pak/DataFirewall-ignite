package com.gpb.datafirewall.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data 
@ConfigurationProperties(prefix = "datafirewall.generated-rules")
public class GeneratedRulesProperties {

    private boolean enabled = true;
    private String outputDir = "./generated-rules";
    private int compilerThreads = 4;
    
}