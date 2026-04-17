package com.gpb.datafirewall.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheUpdateMessage {

    private String cacheName;
    private Integer version;
}
