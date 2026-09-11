package com.muwenyan.simplemap.core.render;

import com.muwenyan.simplemap.core.map.MapCell;
import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.TileKey;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class RenderPlanner {
    private RenderPlanner() {
    }

    public static RenderPlan plan(MapRegion region, int lod, long generation) {
        Objects.requireNonNull(region, "region");
        if (lod < 0 || lod > 31 || generation < 0) {
            throw new IllegalArgumentException("invalid render plan arguments");
        }
        List<RenderTile> tiles = new ArrayList<>();
        ChunkPos origin = region.origin();
        for (int z = 0; z < 32; z++) {
            for (int x = 0; x < 32; x++) {
                MapCell cell = region.cell(x, z);
                if (cell == null || !cell.complete()) {
                    continue;
                }
                TileKey key = new TileKey(region.dimension(),
                        new com.muwenyan.simplemap.core.model.RegionPos(origin.x(), origin.z()), lod, x, z);
                tiles.add(new RenderTile(key, region.revision(), 16, 16));
            }
        }
        tiles.sort(Comparator.comparingInt((RenderTile tile) -> tile.key().x())
                .thenComparingInt(tile -> tile.key().z()));
        return new RenderPlan(region.dimension(), generation, region.revision(), tiles);
    }
}
