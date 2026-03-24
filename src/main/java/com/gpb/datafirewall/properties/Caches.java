package com.gpb.datafirewall.properties;

public enum Caches {
    COMPILED_RULES("compiled_rules"),
    POLITICS_DATASET2CONTROL_AREA("politics_dataset2control_area"),
    DQCHECKS_HASH_CACHE("dqchecks_hash");

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
