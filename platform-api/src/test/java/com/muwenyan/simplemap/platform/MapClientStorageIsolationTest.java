package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.model.DimensionId;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapClientStorageIsolationTest {
    @TempDir Path temporary;

    @Test
    void changingStorageRootClearsPreviousWaypoints() {
        MapClientController controller = new MapClientController();
        DimensionId dimension = new DimensionId("minecraft:overworld");
        UUID actor = UUID.randomUUID();
        controller.bindWaypointStorage(temporary.resolve("one"));
        controller.executeWaypointCommand(actor, dimension, "waypoint add one 1 2 3");
        controller.bindWaypointStorage(temporary.resolve("two"));
        assertEquals(0, controller.visibleWaypoints(dimension).size());
    }
}
