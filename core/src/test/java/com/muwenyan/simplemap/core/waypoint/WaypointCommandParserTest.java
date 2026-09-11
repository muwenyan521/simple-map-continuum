package com.muwenyan.simplemap.core.waypoint;

import com.muwenyan.simplemap.core.model.BlockPos;
import java.util.UUID;
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
}
