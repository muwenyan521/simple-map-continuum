package com.muwenyan.simplemap.core.waypoint;

import com.muwenyan.simplemap.core.navigation.Waypoint;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class WaypointStore {
    private final List<Waypoint> values = new ArrayList<>();
    public List<Waypoint> all() { return List.copyOf(values); }
    public List<Waypoint> visible(DimensionId dimension) {
        return values.stream().filter(w -> w.visible() && w.dimension().equals(dimension))
                .sorted(Comparator.comparing(Waypoint::name).thenComparing(Waypoint::id)).toList();
    }
    public void upsert(Waypoint waypoint) {
        values.removeIf(existing -> existing.id().equals(waypoint.id()));
        values.add(waypoint);
    }
    public boolean remove(UUID id) { return values.removeIf(w -> w.id().equals(id)); }
    public void clearDimension(DimensionId dimension) { values.removeIf(w -> w.dimension().equals(dimension)); }
}
