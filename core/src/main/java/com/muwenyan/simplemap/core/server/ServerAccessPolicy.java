package com.muwenyan.simplemap.core.server;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ServerAccessPolicy {
    private final Set<UUID> operators;
    private final Set<ServerPermission> defaultPermissions;

    public ServerAccessPolicy(Set<UUID> operators, Set<ServerPermission> defaultPermissions) {
        this.operators = Set.copyOf(Objects.requireNonNull(operators, "operators"));
        this.defaultPermissions = Set.copyOf(Objects.requireNonNull(defaultPermissions, "defaultPermissions"));
    }

    public boolean allows(UUID player, ServerPermission permission) {
        Objects.requireNonNull(permission, "permission");
        return player != null && (operators.contains(player) || defaultPermissions.contains(permission));
    }

    public static ServerAccessPolicy permissive() {
        return new ServerAccessPolicy(Set.of(), EnumSet.allOf(ServerPermission.class));
    }
}
