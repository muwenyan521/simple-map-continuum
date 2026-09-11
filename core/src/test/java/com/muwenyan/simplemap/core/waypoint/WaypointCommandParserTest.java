package com.muwenyan.simplemap.core.waypoint;

import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.navigation.Waypoint;
import java.util.UUID;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WaypointCommandParserTest {
    @Test
    void parsesWaypointOperations() {
        WaypointCommand.Add add = (WaypointCommand.Add) WaypointCommandParser.parse("waypoint add Home -1 64 2");
        assertEquals(new BlockPos(-1, 64, 2), add.position());
        UUID id = UUID.randomUUID();
        assertEquals(new WaypointCommand.Remove(id), WaypointCommandParser.parse("waypoint remove " + id));
        assertEquals(new WaypointCommand.ListAll(), WaypointCommandParser.parse("waypoint list"));
    }

    @Test
    void rejectsMalformedCommands() {
        assertThrows(IllegalArgumentException.class, () -> WaypointCommandParser.parse("map list"));
        assertThrows(IllegalArgumentException.class, () -> WaypointCommandParser.parse("waypoint add x 1 2"));
    }

    @Test
    void executesAddAndRemoveAgainstStore() {
        WaypointStore store = new WaypointStore();
        WaypointCommandExecutor executor = new WaypointCommandExecutor(store);
        UUID actor = UUID.randomUUID();
        DimensionId dimension = new DimensionId("minecraft:overworld");
        List<Waypoint> values = executor.execute(actor, dimension,
                WaypointCommandParser.parse("waypoint add Home 1 64 2"));
        assertEquals(1, values.size());
        executor.execute(actor, dimension, new WaypointCommand.Remove(values.get(0).id()));
        assertEquals(0, store.all().size());
    }
}
