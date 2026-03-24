package com.gpb.datafirewall.service;

import org.springframework.stereotype.Service;

import com.gpb.datafirewall.service.impl.DqChecksCacheRefreshServiceImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CacheRefreshService {
    DqChecksCacheRefreshServiceImpl dqChecksRefreshService;

    public void refreshCache() {
        dqChecksRefreshService.refreshCaches();

    };
}
