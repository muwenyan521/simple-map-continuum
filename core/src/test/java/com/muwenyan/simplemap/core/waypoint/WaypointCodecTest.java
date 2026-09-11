package com.muwenyan.simplemap.core.waypoint;

import com.muwenyan.simplemap.core.navigation.Waypoint;
import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WaypointCodecTest {
    @Test
    void roundTripPreservesWaypointData() throws Exception {
        Waypoint waypoint = new Waypoint(UUID.randomUUID(), new DimensionId("minecraft:overworld"),
                new BlockPos(-3, 70, 12), "Home", true);
        assertEquals(List.of(waypoint), WaypointCodec.decode(WaypointCodec.encode(List.of(waypoint))));
    }

    @Test
    void malformedAndTrailingDataFail() throws Exception {
        byte[] encoded = WaypointCodec.encode(List.of());
        byte[] trailing = java.util.Arrays.copyOf(encoded, encoded.length + 1);
        assertThrows(Exception.class, () -> WaypointCodec.decode(trailing));
    }
}
