package com.gpb.datafirewall.service;

import org.springframework.stereotype.Service;

import com.gpb.datafirewall.service.impl.DqChecksCacheRefreshServiceImpl;
import com.gpb.datafirewall.service.impl.PoliticsCacheRefreshServiceImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CacheRefreshService {
    private final DqChecksCacheRefreshServiceImpl dqChecksRefreshService;
    private final PoliticsCacheRefreshServiceImpl politicsCacheRefreshServiceImpl;

    public void refreshCache() {
        dqChecksRefreshService.refreshCaches();
        politicsCacheRefreshServiceImpl.refreshCaches();
    };
}
