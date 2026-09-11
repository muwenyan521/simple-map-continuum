package com.muwenyan.simplemap.core.navigation;

import com.muwenyan.simplemap.core.model.MapMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapModeControllerTest {
    @Test
    void togglesSurfaceAndCaveModes() {
        MapModeController controller = new MapModeController(MapMode.SURFACE);
        assertEquals(MapMode.CAVE, controller.toggle());
        assertEquals(MapMode.SURFACE, controller.toggle());
    }
}
