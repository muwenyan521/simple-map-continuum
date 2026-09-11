package com.muwenyan.simplemap.core.navigation;

import com.muwenyan.simplemap.core.model.DimensionId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerMapStateTest {
    @Test
    void floorsWorldCoordinatesForDisplay() {
        PlayerMapState state = new PlayerMapState(new DimensionId("minecraft:overworld"), -0.2, 3.9, 64, 90);
        assertEquals(-1, state.blockPosition().x());
        assertEquals(3, state.blockPosition().z());
    }
}
