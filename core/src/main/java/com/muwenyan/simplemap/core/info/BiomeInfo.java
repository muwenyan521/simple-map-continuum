package com.muwenyan.simplemap.core.info;

import java.util.Objects;

public record BiomeInfo(String id, String displayName, float temperature, float downfall) {
    public BiomeInfo {
        id = Objects.requireNonNull(id, "id").trim();
        displayName = Objects.requireNonNull(displayName, "displayName").trim();
        if (id.isEmpty() || displayName.isEmpty() || !Float.isFinite(temperature)
                || !Float.isFinite(downfall) || downfall < 0f || downfall > 1f) {
            throw new IllegalArgumentException("invalid biome info");
        }
    }
}
