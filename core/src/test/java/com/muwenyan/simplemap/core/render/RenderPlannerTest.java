package com.muwenyan.simplemap.core.render;

import com.muwenyan.simplemap.core.map.MapCell;
import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RenderPlannerTest {
    @Test
    void planContainsOnlyCompleteCellsInStableOrder() {
        MapRegion region = new MapRegion(new DimensionId("minecraft:overworld"), new ChunkPos(-2, 3), 32, 32);
        region.apply(4, 2, new MapCell(new BlockPos(-28, 70, 50), 0xff336699, 4, false, true));
        region.apply(1, 1, new MapCell(new BlockPos(-31, 64, 33), 0xff336699, 2, false, false));
        RenderPlan plan = RenderPlanner.plan(region, 2, 9);
        assertEquals(1, plan.tiles().size());
        assertEquals(4, plan.tiles().get(0).key().x());
        assertEquals(9, plan.frame().generation());
    }
}
