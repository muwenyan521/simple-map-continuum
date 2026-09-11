package com.muwenyan.simplemap.core.config;

import com.muwenyan.simplemap.core.cave.CaveConfig;
import com.muwenyan.simplemap.core.feature.MapFeatureFlags;
import com.muwenyan.simplemap.core.minimap.MinimapConfig;
import com.muwenyan.simplemap.core.style.MapStyle;
import java.util.Objects;

public record ClientMapConfig(MapConfig map, MinimapConfig minimap, CaveConfig cave,
                              MapStyle style, MapFeatureFlags features) {
    public ClientMapConfig {
        map = Objects.requireNonNull(map, "map");
        minimap = Objects.requireNonNull(minimap, "minimap");
        cave = Objects.requireNonNull(cave, "cave");
        style = Objects.requireNonNull(style, "style");
        features = Objects.requireNonNull(features, "features");
    }

    public static ClientMapConfig defaults() {
        return new ClientMapConfig(MapConfig.defaults(), MinimapConfig.defaults(), CaveConfig.defaults(),
                MapStyle.defaults(), MapFeatureFlags.defaults());
    }
}
