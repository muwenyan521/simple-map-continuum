package com.muwenyan.simplemap.core.minimap;

import com.muwenyan.simplemap.core.model.ChunkPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinimapProjectionTest {
    @Test
    void centerAndRotationAreDeterministic() {
        MinimapProjection.ScreenPoint center = MinimapProjection.worldToScreen(
                100, 200, 100, 200, 2, 90, true, 128);
        assertEquals(64d, center.x());
        assertEquals(64d, center.z());

        MinimapProjection.ScreenPoint east = MinimapProjection.worldToScreen(
                101, 200, 100, 200, 2, 90, true, 128);
        assertEquals(64d, east.x(), 1e-9);
        assertEquals(62d, east.z(), 1e-9);
    }

    @Test
    void shapeVisibilityAndChunkCenter() {
        MinimapProjection.ScreenPoint corner = new MinimapProjection.ScreenPoint(127, 127);
        assertTrue(MinimapProjection.visible(corner, 128, MinimapShape.SQUARE));
        assertFalse(MinimapProjection.visible(corner, 128, MinimapShape.CIRCLE));
        MinimapProjection.ScreenPoint tile = MinimapProjection.chunkCenterToScreen(
                new ChunkPos(0, 0), 8, 8, 1, 0, false, 128);
        assertEquals(64d, tile.x());
        assertEquals(64d, tile.z());
    }

    @Test
    void configRejectsUnsafeValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new MinimapConfig(true, 32, 1, MinimapShape.CIRCLE,
                        MinimapAnchor.TOP_RIGHT, false, false));
        assertThrows(IllegalArgumentException.class,
                () -> new MinimapConfig(true, 128, Double.NaN, MinimapShape.CIRCLE,
                        MinimapAnchor.TOP_RIGHT, false, false));
    }
}
