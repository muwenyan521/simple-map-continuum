package com.muwenyan.simplemap.core.server;

import com.muwenyan.simplemap.core.book.MapBook;
import com.muwenyan.simplemap.core.model.RegionPos;
import java.util.Objects;
import java.util.UUID;

public final class ServerMapService {
    private final ServerMapConfig config;
    private final ServerAccessPolicy access;

    public ServerMapService(ServerMapConfig config, ServerAccessPolicy access) {
        this.config = Objects.requireNonNull(config, "config");
        this.access = Objects.requireNonNull(access, "access");
    }

    public ServerMapConfig config() {
        return config;
    }

    public void require(UUID player, ServerPermission permission) {
        if (!config.enabled() || !access.allows(player, permission)) {
            throw new SecurityException("server map permission denied: " + permission);
        }
    }

    public void saveRegion(UUID player, MapBook book, RegionPos position, byte[] payload) {
        require(player, ServerPermission.MAP_SAVE);
        if (!config.allowMapBooks() || book.snapshot().regions().size() >= config.maxRegionsPerBook()) {
            throw new IllegalStateException("map book limit reached");
        }
        if (payload == null || payload.length > config.maxPayloadBytes()) {
            throw new IllegalArgumentException("payload exceeds server limit");
        }
        book.save(player, position, payload);
    }
}
