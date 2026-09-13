package com.muwenyan.simplemap.core.surface;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SurfaceTintTest {
    @Test
    void appliesBiomeTintToBaseColorAndPreservesAlpha() {
        assertEquals(0xff202000, SurfaceTint.apply(0xffff8000, 0xff2040c0));
        assertEquals(0xff112233, SurfaceTint.apply(0xff112233, 0xff112233));
    }
}
