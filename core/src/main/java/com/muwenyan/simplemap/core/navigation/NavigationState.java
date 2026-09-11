package com.muwenyan.simplemap.core.navigation;

import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class NavigationState {
    private MapViewport viewport;
    private NavigationPin pin;
    private UUID followedWaypoint;
    private final List<Waypoint> waypoints = new ArrayList<>();

    public NavigationState(MapViewport viewport) { this.viewport = Objects.requireNonNull(viewport, "viewport"); }
    public synchronized MapViewport viewport() { return viewport; }
    public synchronized NavigationPin pin() { return pin; }
    public synchronized List<Waypoint> waypoints() { return List.copyOf(waypoints); }
    public synchronized Optional<Waypoint> followedWaypoint() {
        return waypoints.stream().filter(value -> value.id().equals(followedWaypoint)).findFirst();
    }
    public synchronized void pan(double x, double z) { viewport = viewport.pan(x, z); }
    public synchronized void zoomAt(double factor, double cursorX, double cursorZ) { viewport = viewport.zoomAt(factor, cursorX, cursorZ); }
    public synchronized void center(double x, double z) { viewport = viewport.centered(x, z); }
    public synchronized void setPin(NavigationPin value) { pin = value; }
    public synchronized void clearPin() { pin = null; }
    public synchronized void upsertWaypoint(Waypoint waypoint) {
        Objects.requireNonNull(waypoint, "waypoint");
        waypoints.removeIf(existing -> existing.id().equals(waypoint.id()));
        waypoints.add(waypoint);
    }
    public synchronized boolean removeWaypoint(UUID id) {
        boolean removed = waypoints.removeIf(value -> value.id().equals(id));
        if (removed && id.equals(followedWaypoint)) followedWaypoint = null;
        return removed;
    }
    public synchronized void followWaypoint(UUID id) {
        if (id == null || waypoints.stream().noneMatch(value -> value.id().equals(id))) {
            throw new IllegalArgumentException("unknown waypoint");
        }
        followedWaypoint = id;
    }
    public synchronized void clearFollowedWaypoint() { followedWaypoint = null; }
    public synchronized void clearDimension(DimensionId dimension) {
        waypoints.removeIf(value -> value.dimension().equals(dimension));
        if (pin != null && pin.dimension().equals(dimension)) pin = null;
        if (followedWaypoint != null && waypoints.stream().noneMatch(value -> value.id().equals(followedWaypoint))) {
            followedWaypoint = null;
        }
    }
}
