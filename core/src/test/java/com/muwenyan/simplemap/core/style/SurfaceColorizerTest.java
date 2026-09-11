package com.muwenyan.simplemap.core.style;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SurfaceColorizerTest {
    @Test
    void overrideReliefAndWaterAreApplied() {
        MapStyle style = new MapStyle(com.muwenyan.simplemap.core.color.ColorProfile.BALANCED,
                ReliefMode.TWO_D, WaterShading.DEPTH, true, Map.of(7, 0xff204060));
        int shaded = SurfaceColorizer.colorize(0xffff0000, 7, 100, 90, 8, style);
        assertNotEquals(0xffff0000, shaded);
        assertEquals(0xff204060, style.blockOverrides().get(7));
    }
}
