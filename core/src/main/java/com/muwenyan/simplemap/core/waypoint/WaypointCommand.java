package com.muwenyan.simplemap.core.waypoint;

import com.muwenyan.simplemap.core.model.BlockPos;
import java.util.Objects;
import java.util.UUID;

public sealed interface WaypointCommand permits WaypointCommand.Add, WaypointCommand.Remove,
        WaypointCommand.Follow, WaypointCommand.ListAll {
    record Add(String name, BlockPos position) implements WaypointCommand {
        public Add {
            name = Objects.requireNonNull(name, "name").trim();
            position = Objects.requireNonNull(position, "position");
            if (name.isEmpty()) {
                throw new IllegalArgumentException("name");
            }
        }
    }

    record Remove(UUID id) implements WaypointCommand {
        public Remove {
            Objects.requireNonNull(id, "id");
        }
    }

    record Follow(UUID id) implements WaypointCommand {
        public Follow {
            Objects.requireNonNull(id, "id");
        }
    }

    record ListAll() implements WaypointCommand { }
}
