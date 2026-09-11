package com.muwenyan.simplemap.core.surface;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackedSurfaceCellTest {
    @Test
    void preservesLegacyBitLayoutAndDerivedWaterValues() {
        long packed = PackedSurfaceCell.pack((short) 80, (short) 12, (byte) 3, (byte) 0x2f, (short) 64);
        assertEquals(80, PackedSurfaceCell.topY(packed));
        assertEquals(12, PackedSurfaceCell.blockId(packed));
        assertEquals(3, PackedSurfaceCell.biomeId(packed));
        assertEquals(16, PackedSurfaceCell.waterDepth(packed));
        assertEquals(64, PackedSurfaceCell.reliefY(packed));
    }
}
