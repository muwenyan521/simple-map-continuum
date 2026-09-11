package com.muwenyan.simplemap.core.navigation;

import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.UUID;

public final class NavigationStateTest {
    private NavigationStateTest() { }
    public static void main(String[] args) {
        DimensionId overworld = new DimensionId("minecraft:overworld");
        NavigationState state = new NavigationState(new MapViewport(0, 0, 1));
        state.pan(10, -4);
        require(state.viewport().centerX() == 10 && state.viewport().centerZ() == -4, "pan");
        state.zoomAt(2, 8, 0);
        require(state.viewport().zoom() == 2 && state.viewport().centerX() == 14, "zoom to cursor");
        UUID id = UUID.randomUUID();
        state.upsertWaypoint(new Waypoint(id, overworld, new BlockPos(1, 64, 2), "home", true));
        state.upsertWaypoint(new Waypoint(id, overworld, new BlockPos(3, 64, 4), "home", false));
        require(state.waypoints().size() == 1 && !state.waypoints().get(0).visible(), "upsert");
        state.followWaypoint(id);
        require(state.followedWaypoint().isPresent(), "follow");
        state.setPin(new NavigationPin(UUID.randomUUID(), overworld, new BlockPos(0, 64, 0), "target"));
        state.clearDimension(overworld);
        require(state.waypoints().isEmpty() && state.pin() == null && state.followedWaypoint().isEmpty(), "dimension clear");
        System.out.println("NAVIGATION_STATE_PASS");
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
