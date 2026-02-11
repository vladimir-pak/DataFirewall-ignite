package com.gpb.datafirewall.service;

/**
 * Верхнеуровневый интерфейс для вызова методов сервиса
 */
public interface SqlParserService {
    void parse(String serviceName);
    void parseAll(String sourceName);

    }
