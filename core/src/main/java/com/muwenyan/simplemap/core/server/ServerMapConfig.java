package com.muwenyan.simplemap.core.server;

import java.util.Objects;

public record ServerMapConfig(boolean enabled, boolean allowMapBooks, boolean allowWaypointSync,
                              int maxRegionsPerBook, int maxPayloadBytes) {
    public ServerMapConfig {
        if (maxRegionsPerBook < 1 || maxRegionsPerBook > 4096
                || maxPayloadBytes < 1024 || maxPayloadBytes > 4 * 1024 * 1024) {
            throw new IllegalArgumentException("invalid server map limits");
        }
    }

    public static ServerMapConfig defaults() {
        return new ServerMapConfig(true, true, true, 256, 4 * 1024 * 1024);
    }
}
