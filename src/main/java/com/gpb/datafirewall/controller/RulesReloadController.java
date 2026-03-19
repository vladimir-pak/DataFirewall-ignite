package com.gpb.datafirewall.controller;

import org.springframework.web.bind.annotation.*;

import com.gpb.datafirewall.service.SqlParserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RulesReloadController {

    private final SqlParserService sqlParserService;

    @PostMapping("/compiled-rules/rebuild")
    public String rebuild(@RequestParam String sourceName) {
        sqlParserService.parseAll(sourceName);
        return "OK";
    }
}

