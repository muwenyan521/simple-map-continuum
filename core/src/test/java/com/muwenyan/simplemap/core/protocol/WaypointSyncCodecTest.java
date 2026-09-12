package com.muwenyan.simplemap.core.protocol;

import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.navigation.Waypoint;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaypointSyncCodecTest {
    @Test
    void roundTripsRevisionAndWaypoints() throws Exception {
        Waypoint waypoint = new Waypoint(UUID.randomUUID(), new DimensionId("minecraft:overworld"),
                new BlockPos(1, 64, 2), "base", true);
        WaypointSyncMessage source = new WaypointSyncMessage(3, List.of(waypoint));
        WaypointSyncMessage decoded = WaypointSyncCodec.decode(WaypointSyncCodec.encode(source));
        assertEquals(source.revision(), decoded.revision());
        assertEquals(source.waypoints(), decoded.waypoints());
    }
}
