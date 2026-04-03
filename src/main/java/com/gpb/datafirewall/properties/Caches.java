package com.gpb.datafirewall.properties;

public enum Caches {
    DQCHECKS_HASH_CACHE("dqchecks_hash"),
    PG_STAT("pg_stat"),
    COMPILED_RULES("compiled_rules"),
    POLITICS_DATASET2CONTROL_AREA("politics_dataset2control_area"),
    POLITICS_CONTROL_AREA_RULES("politics_control_area_rules"),
    POLITICS_ERROR_MESSAGES("politics_error_messages"),
    POLITICS_DATASET_EXCLUSION("politics_dataset_exclusion");

    private final String name;

    Caches(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Caches fromString(String name) {
        for (Caches dbObjectType : Caches.values()) {
            if (dbObjectType.name.equalsIgnoreCase(name)) {
                return dbObjectType;
            }
        }
        throw new IllegalArgumentException("Unknown entity: " + name);
    }
}
