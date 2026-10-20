package com.gpb.datafirewall.service;

import com.gpb.datafirewall.model.CacheVersion;
import com.gpb.datafirewall.model.CacheVersionId;
import com.gpb.datafirewall.repository.CacheVersionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CacheVersionPublisher {

    private final CacheVersionRepository cacheVersionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publish(String cacheName, Integer version) {
        cacheVersionRepository.saveAndFlush(
                new CacheVersion(
                        new CacheVersionId(cacheName, version)
                )
        );
    }
}
