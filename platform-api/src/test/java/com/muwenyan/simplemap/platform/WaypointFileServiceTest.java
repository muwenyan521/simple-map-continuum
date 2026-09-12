package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.navigation.Waypoint;
import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.waypoint.WaypointStore;
import com.muwenyan.simplemap.platform.file.WaypointFileService;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaypointFileServiceTest {
    @TempDir Path temporary;

    @Test
    void persistsWaypointStoreWithBackup() throws Exception {
        WaypointStore store = new WaypointStore();
        store.upsert(new Waypoint(UUID.randomUUID(), new DimensionId("minecraft:overworld"), new BlockPos(1, 2, 3), "home", true));
        WaypointFileService service = new WaypointFileService(temporary);
        service.write(store);
        WaypointStore loaded = new WaypointStore();
        service.readInto(loaded);
        assertEquals(1, loaded.all().size());
    }
}
