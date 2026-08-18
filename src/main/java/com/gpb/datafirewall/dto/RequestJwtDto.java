package com.gpb.datafirewall.dto;

import lombok.Data;

@Data
public class RequestJwtDto {
    private String secret;
    private String service;
}
