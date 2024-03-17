package dev.jay.kacore.redis.entries;

import java.util.UUID;

public class StaffEntry {
    private final String name;
    private final UUID id;

    public StaffEntry(String name, UUID id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }
}