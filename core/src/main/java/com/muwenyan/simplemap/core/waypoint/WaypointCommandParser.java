package com.muwenyan.simplemap.core.waypoint;

import com.muwenyan.simplemap.core.model.BlockPos;
import java.util.UUID;

public final class WaypointCommandParser {
    private WaypointCommandParser() {
    }

    public static WaypointCommand parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("command is empty");
        }
        String[] tokens = input.trim().split("\\s+");
        if (tokens.length < 2 || !"waypoint".equalsIgnoreCase(tokens[0])) {
            throw new IllegalArgumentException("expected waypoint command");
        }
        return switch (tokens[1].toLowerCase(java.util.Locale.ROOT)) {
            case "list" -> requireLength(tokens, 2, new WaypointCommand.ListAll());
            case "remove" -> requireLength(tokens, 3, new WaypointCommand.Remove(uuid(tokens[2])));
            case "follow" -> requireLength(tokens, 3, new WaypointCommand.Follow(uuid(tokens[2])));
            case "add" -> {
                if (tokens.length != 6) {
                    throw new IllegalArgumentException("add requires name x y z");
                }
                yield new WaypointCommand.Add(tokens[2], new BlockPos(integer(tokens[3]), integer(tokens[4]), integer(tokens[5])));
            }
            default -> throw new IllegalArgumentException("unknown waypoint command");
        };
    }

    private static <T extends WaypointCommand> T requireLength(String[] tokens, int expected, T command) {
        if (tokens.length != expected) {
            throw new IllegalArgumentException("unexpected command arguments");
        }
        return command;
    }

    private static UUID uuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("invalid waypoint id", exception);
        }
    }

    private static int integer(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("invalid coordinate", exception);
        }
    }
}
