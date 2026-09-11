package com.muwenyan.simplemap.core.waypoint;

import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.navigation.Waypoint;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class WaypointCommandExecutor {
    private final WaypointStore store;

    public WaypointCommandExecutor(WaypointStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public List<Waypoint> execute(UUID actor, DimensionId dimension, WaypointCommand command) {
        Objects.requireNonNull(actor, "actor");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(command, "command");
        if (command instanceof WaypointCommand.Add add) {
            store.upsert(new Waypoint(UUID.randomUUID(), dimension, add.position(), add.name(), true));
        } else if (command instanceof WaypointCommand.Remove remove) {
            store.remove(remove.id());
        } else if (command instanceof WaypointCommand.Follow follow) {
            store.visible(dimension).stream().filter(value -> value.id().equals(follow.id()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("waypoint is not visible"));
        }
        return store.visible(dimension);
    }
}
