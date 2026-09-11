package com.muwenyan.simplemap.core.feature;

public record MapFeatureFlags(
        boolean fullscreenMap,
        boolean minimap,
        boolean waypoints,
        boolean blockInformation,
        boolean biomeInformation,
        boolean caveMap,
        boolean terrainRelief,
        boolean waterShading,
        boolean flowers,
        boolean mapBook,
        boolean debugOverlay) {
    public static MapFeatureFlags defaults() {
        return new MapFeatureFlags(true, true, true, true, true, true, true, true, true, true, false);
    }
}
