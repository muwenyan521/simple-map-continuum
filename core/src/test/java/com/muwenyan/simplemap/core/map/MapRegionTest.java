package com.muwenyan.simplemap.core.map;

import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;

public final class MapRegionTest {
    private MapRegionTest() { }
    public static void main(String[] args) {
        MapRegion region = new MapRegion(new DimensionId("minecraft:overworld"), new ChunkPos(-1, 2), 32, 32);
        MapCell cell = new MapCell(new BlockPos(1, 64, 2), 0xff33aa55, 64, false, true);
        region.apply(0, 0, cell);
        if (region.cell(0, 0) != cell || region.revision() != 1 || region.completedCells().size() != 1) {
            throw new AssertionError("map region state");
        }
        System.out.println("MAP_REGION_PASS");
    }
}
