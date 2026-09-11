package com.muwenyan.simplemap.core.navigation;

import com.muwenyan.simplemap.core.model.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoordinateDisplayTest {
    @Test
    void formatsNegativeCoordinatesWithFloorDivision() {
        BlockPos position = new BlockPos(-1, 64, -17);
        assertEquals("-1, 64, -17", CoordinateDisplay.format(position, CoordinateFormat.BLOCK));
        assertEquals("-1, -2", CoordinateDisplay.format(position, CoordinateFormat.CHUNK));
        assertEquals("-1, -1", CoordinateDisplay.format(position, CoordinateFormat.REGION));
    }

    @Test
    void mapsYawToCardinalDirection() {
        assertEquals(CompassDirection.NORTH, CompassDirection.fromYaw(0));
        assertEquals(CompassDirection.EAST, CompassDirection.fromYaw(90));
        assertEquals(CompassDirection.WEST, CompassDirection.fromYaw(-90));
    }
}
