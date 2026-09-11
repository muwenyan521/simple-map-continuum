package com.muwenyan.simplemap.core.minimap;

import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.navigation.PlayerMapState;
import java.util.List;
import java.util.Objects;

public final class MinimapFrameBuilder {
    private MinimapFrameBuilder() {
    }

    public static MinimapFrame build(PlayerMapState player, MinimapConfig config,
                                     long generation, List<MinimapTile> tiles) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(tiles, "tiles");
        DimensionId dimension = player.dimension();
        return new MinimapFrame(dimension, generation, player.x(), player.z(), player.yaw(), config, tiles);
    }
}
