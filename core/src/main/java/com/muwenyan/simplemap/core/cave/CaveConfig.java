package com.muwenyan.simplemap.core.cave;

public record CaveConfig(CaveMode mode, int topY, int maxLayers, CaveLightMode lightMode) {
    public CaveConfig {
        if (mode == null || lightMode == null) {
            throw new NullPointerException("mode/lightMode");
        }
        if (topY < -64 || topY > 512 || maxLayers < 1 || maxLayers > 32) {
            throw new IllegalArgumentException("invalid cave bounds");
        }
    }

    public static CaveConfig defaults() {
        return new CaveConfig(CaveMode.AUTO, 320, 8, CaveLightMode.BRIGHT);
    }
}
