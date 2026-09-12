package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.model.MapMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapClientControllerTest {
    @Test
    void togglesModeThroughSharedRuntime() {
        MapClientController controller = new MapClientController();
        assertEquals(MapMode.SURFACE, controller.mode());
        assertEquals(MapMode.CAVE, controller.toggleMode());
        assertEquals(MapMode.CAVE, controller.runtime().mode().mode());
    }
}
