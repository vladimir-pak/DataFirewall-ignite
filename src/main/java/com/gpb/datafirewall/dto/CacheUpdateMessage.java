package com.gpb.datafirewall.dto;

import lombok.Data;

@Data
public class CacheUpdateMessage {

    private String cacheName;
    private Integer version;
    private String handler;

    public CacheUpdateMessage(String cacheName, Integer version) {
        this.cacheName = cacheName;
        this.version = version;
    }

    public CacheUpdateMessage(String cacheName, Integer version, String handler) {
        this.cacheName = cacheName;
        this.version = version;
        this.handler = handler;
    }
}
