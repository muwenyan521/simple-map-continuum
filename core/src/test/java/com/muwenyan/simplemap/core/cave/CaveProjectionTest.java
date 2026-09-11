package com.muwenyan.simplemap.core.cave;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaveProjectionTest {
    @Test
    void modeControlsSelectionAndLight() {
        CaveLayer low = new CaveLayer(20, 10, 0xff808080, 0, false, false);
        CaveLayer high = new CaveLayer(80, 60, 0xffff0000, 15, false, true);
        CaveConfig auto = new CaveConfig(CaveMode.AUTO, 320, 8, CaveLightMode.BRIGHT);
        assertEquals(low.topY(), CaveProjection.select(List.of(low, high), 30, auto).orElseThrow().topY());
        CaveConfig on = new CaveConfig(CaveMode.ON, 320, 8, CaveLightMode.RAW);
        assertEquals(high.argb(), CaveProjection.select(List.of(low, high), 30, on).orElseThrow().argb());
        assertTrue(CaveProjection.select(List.of(low), 30,
                new CaveConfig(CaveMode.OFF, 320, 8, CaveLightMode.RAW)).isEmpty());
    }

    @Test
    void layerLimitIsDeterministic() {
        List<CaveLayer> layers = List.of(
                new CaveLayer(0, -10, 0xff202020, 1, false, false),
                new CaveLayer(30, 20, 0xff303030, 2, false, false),
                new CaveLayer(60, 50, 0xff404040, 3, false, false));
        List<CaveLayer> result = CaveProjection.limitAndShade(layers, 35,
                new CaveConfig(CaveMode.ON, 320, 2, CaveLightMode.DIM));
        assertEquals(2, result.size());
        assertEquals(30, result.get(0).topY());
    }
}
