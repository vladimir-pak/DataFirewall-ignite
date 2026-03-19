package com.gpb.datafirewall.service.impl;

import com.gpb.datafirewall.service.GeneratedRuleSourceFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class GeneratedRuleSourceFileServiceImpl implements GeneratedRuleSourceFileService {

    private final Path outputDir;
    private final boolean enabled;

    public GeneratedRuleSourceFileServiceImpl(
            @Value("${datafirewall.generated-rules.enabled:true}") boolean enabled,
            @Value("${datafirewall.generated-rules.output-dir:generated-rules}") String outputDir
    ) {
        this.enabled = enabled;
        this.outputDir = Path.of(outputDir);
    }

    @Override
    public void save(String className, String javaSource) {
        if (!enabled || className == null || className.isBlank() || javaSource == null) {
            return;
        }

        try {
            Files.createDirectories(outputDir);
            Path file = outputDir.resolve(className + ".java");
            Files.writeString(file, javaSource, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to save generated rule source for {}", className, e);
        }
    }
}