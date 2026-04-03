package com.gpb.datafirewall.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CacheResponseDto<K, V> {
    private String cacheName;
    private int count;
    private Map<K, V> cache;
}
