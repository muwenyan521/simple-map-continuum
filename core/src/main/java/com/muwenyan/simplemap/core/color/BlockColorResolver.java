package com.muwenyan.simplemap.core.color;

import java.util.Map;
import java.util.Objects;

public final class BlockColorResolver {
    private final Map<String, Integer> overrides;
    private final ColorProfile profile;

    public BlockColorResolver(Map<String, Integer> overrides, ColorProfile profile) {
        this.overrides = Map.copyOf(Objects.requireNonNull(overrides, "overrides"));
        this.profile = Objects.requireNonNull(profile, "profile");
        for (Map.Entry<String, Integer> entry : this.overrides.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null
                    || (entry.getValue() >>> 24) == 0) {
                throw new IllegalArgumentException("invalid color override");
            }
        }
    }

    public int resolve(String blockId, int vanillaArgb) {
        Objects.requireNonNull(blockId, "blockId");
        return profile.apply(overrides.getOrDefault(blockId, vanillaArgb));
    }
}
