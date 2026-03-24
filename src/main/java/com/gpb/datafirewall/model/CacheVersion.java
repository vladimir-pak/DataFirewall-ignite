package com.gpb.datafirewall.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cache_version", schema = "datafirewall")
public class CacheVersion {

    @EmbeddedId
    private CacheVersionId id;
}
