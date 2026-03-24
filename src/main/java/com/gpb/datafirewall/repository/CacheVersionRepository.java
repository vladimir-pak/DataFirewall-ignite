package com.gpb.datafirewall.repository;

import com.gpb.datafirewall.model.CacheVersion;
import com.gpb.datafirewall.model.CacheVersionId;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CacheVersionRepository extends JpaRepository<CacheVersion, CacheVersionId> {
    Optional<CacheVersion> findTopByIdCacheNameOrderByIdVersionDesc(String cacheName);
    void deleteByCacheName(String cacheName);
}
