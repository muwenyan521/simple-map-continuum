package com.muwenyan.simplemap.core.minimap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinimapLayoutTest {
    @Test
    void placesMinimapAtConfiguredAnchor() {
        MinimapConfig config = new MinimapConfig(true, 128, 1, MinimapShape.CIRCLE,
                MinimapAnchor.BOTTOM_RIGHT, false, true);
        assertEquals(new MinimapLayout.ScreenRect(852, 452, 128, 128),
                MinimapLayout.place(config, 1000, 600, 20));
        assertThrows(IllegalArgumentException.class, () -> MinimapLayout.place(config, 100, 100, 20));
    }
}
