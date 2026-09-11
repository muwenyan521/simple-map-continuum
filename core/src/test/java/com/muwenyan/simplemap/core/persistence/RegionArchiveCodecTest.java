package com.muwenyan.simplemap.core.persistence;

import com.muwenyan.simplemap.core.map.MapCell;
import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegionArchiveCodecTest {
    @Test
    void roundTripPreservesRegionCells() throws Exception {
        MapRegion source = new MapRegion(new DimensionId("minecraft:overworld"), new ChunkPos(-4, 7), 32, 32);
        MapCell cell = new MapCell(new BlockPos(-56, 72, 120), 0xff336699, 12, true, true);
        source.apply(3, 5, cell);
        MapRegion decoded = RegionArchiveCodec.decode(
                RegionArchiveCodec.encode(source, ArchiveFormat.SMAP), ArchiveFormat.SMAP);
        assertEquals(source.dimension(), decoded.dimension());
        assertEquals(source.origin(), decoded.origin());
        assertEquals(cell, decoded.cell(3, 5));
    }

    @Test
    void malformedPayloadIsRejected() throws Exception {
        MapRegion source = new MapRegion(new DimensionId("minecraft:overworld"), new ChunkPos(0, 0), 32, 32);
        byte[] encoded = RegionArchiveCodec.encode(source, ArchiveFormat.SMAP);
        encoded[encoded.length - 1] ^= 1;
        assertThrows(ArchiveException.class, () -> RegionArchiveCodec.decode(encoded, ArchiveFormat.SMAP));
    }
}
