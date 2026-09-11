package com.muwenyan.simplemap.core.feature;

import java.util.Objects;

public record FeatureAvailability(MapFeatureFlags enabled, MapFeatureFlags supported) {
    public FeatureAvailability {
        enabled = Objects.requireNonNull(enabled, "enabled");
        supported = Objects.requireNonNull(supported, "supported");
        if (enabled.fullscreenMap() && !supported.fullscreenMap()
                || enabled.minimap() && !supported.minimap()
                || enabled.waypoints() && !supported.waypoints()
                || enabled.blockInformation() && !supported.blockInformation()
                || enabled.biomeInformation() && !supported.biomeInformation()
                || enabled.caveMap() && !supported.caveMap()
                || enabled.terrainRelief() && !supported.terrainRelief()
                || enabled.waterShading() && !supported.waterShading()
                || enabled.flowers() && !supported.flowers()
                || enabled.mapBook() && !supported.mapBook()
                || enabled.debugOverlay() && !supported.debugOverlay()) {
            throw new IllegalArgumentException("enabled feature is unsupported");
        }
    }
}
