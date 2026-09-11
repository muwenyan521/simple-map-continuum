package com.muwenyan.simplemap.core.waypoint;

import com.muwenyan.simplemap.core.navigation.Waypoint;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class WaypointStore {
    private final List<Waypoint> values = new ArrayList<>();
    public synchronized List<Waypoint> all() { return List.copyOf(values); }
    public synchronized List<Waypoint> visible(DimensionId dimension) {
        return values.stream().filter(w -> w.visible() && w.dimension().equals(dimension))
                .sorted(Comparator.comparing(Waypoint::name).thenComparing(Waypoint::id)).toList();
    }
    public synchronized void upsert(Waypoint waypoint) {
        if (waypoint == null) throw new NullPointerException("waypoint");
        values.removeIf(existing -> existing.id().equals(waypoint.id()));
        values.add(waypoint);
    }
    public synchronized boolean remove(UUID id) {
        if (id == null) throw new NullPointerException("id");
        return values.removeIf(w -> w.id().equals(id));
    }
    public synchronized void clearDimension(DimensionId dimension) {
        if (dimension == null) throw new NullPointerException("dimension");
        values.removeIf(w -> w.dimension().equals(dimension));
    }
}
