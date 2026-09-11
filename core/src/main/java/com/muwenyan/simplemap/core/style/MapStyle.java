package com.muwenyan.simplemap.core.style;

import com.muwenyan.simplemap.core.color.ColorProfile;
import java.util.Map;

public record MapStyle(ColorProfile colorProfile, ReliefMode relief, WaterShading water,
                       boolean flowers, Map<Integer, Integer> blockOverrides) {
    public MapStyle {
        if (colorProfile == null || relief == null || water == null || blockOverrides == null) {
            throw new NullPointerException("style values");
        }
        for (Map.Entry<Integer, Integer> entry : blockOverrides.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || (entry.getValue() >>> 24) == 0) {
                throw new IllegalArgumentException("invalid block override");
            }
        }
        blockOverrides = Map.copyOf(blockOverrides);
    }

    public static MapStyle defaults() {
        return new MapStyle(ColorProfile.BALANCED, ReliefMode.TWO_D, WaterShading.DEPTH, true, Map.of());
    }
}
