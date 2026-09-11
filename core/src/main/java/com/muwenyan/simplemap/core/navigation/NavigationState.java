package com.muwenyan.simplemap.core.navigation;

import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class NavigationState {
    private MapViewport viewport;
    private NavigationPin pin;
    private final List<Waypoint> waypoints = new ArrayList<>();

    public NavigationState(MapViewport viewport) { this.viewport = Objects.requireNonNull(viewport, "viewport"); }
    public MapViewport viewport() { return viewport; }
    public NavigationPin pin() { return pin; }
    public List<Waypoint> waypoints() { return List.copyOf(waypoints); }
    public void pan(double x, double z) { viewport = viewport.pan(x, z); }
    public void zoomAt(double factor, double cursorX, double cursorZ) { viewport = viewport.zoomAt(factor, cursorX, cursorZ); }
    public void center(double x, double z) { viewport = viewport.centered(x, z); }
    public void setPin(NavigationPin value) { pin = value; }
    public void clearPin() { pin = null; }
    public void upsertWaypoint(Waypoint waypoint) {
        Objects.requireNonNull(waypoint, "waypoint");
        waypoints.removeIf(existing -> existing.id().equals(waypoint.id()));
        waypoints.add(waypoint);
    }
    public boolean removeWaypoint(UUID id) { return waypoints.removeIf(value -> value.id().equals(id)); }
    public void clearDimension(DimensionId dimension) {
        waypoints.removeIf(value -> value.dimension().equals(dimension));
        if (pin != null && pin.dimension().equals(dimension)) pin = null;
    }
}
