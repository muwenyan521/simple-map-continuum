package com.muwenyan.simplemap.core.protocol;

import com.muwenyan.simplemap.core.navigation.Waypoint;
import java.util.List;
import java.util.Objects;

public record WaypointSyncMessage(long revision, List<Waypoint> waypoints) {
    public WaypointSyncMessage {
        if (revision < 0 || waypoints == null) throw new IllegalArgumentException("invalid waypoint sync");
        waypoints = List.copyOf(waypoints);
    }
}
