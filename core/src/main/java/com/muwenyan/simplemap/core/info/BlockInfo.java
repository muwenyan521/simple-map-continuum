package com.muwenyan.simplemap.core.info;

import java.util.Objects;

public record BlockInfo(String id, String displayName, int colorArgb, boolean fluid, boolean solid) {
    public BlockInfo {
        id = normalize(id, "id");
        displayName = normalize(displayName, "displayName");
        if ((colorArgb >>> 24) == 0) {
            throw new IllegalArgumentException("block color must be visible");
        }
    }

    private static String normalize(String value, String field) {
        Objects.requireNonNull(value, field);
        String normalized = value.trim();
        if (normalized.isEmpty() || normalized.length() > 256) {
            throw new IllegalArgumentException("invalid " + field);
        }
        return normalized;
    }
}
