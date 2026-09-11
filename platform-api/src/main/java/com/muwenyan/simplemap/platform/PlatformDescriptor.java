package com.muwenyan.simplemap.platform;

import java.util.Objects;

public record PlatformDescriptor(LoaderPlatform loader, String minecraftVersion, int javaVersion) {
    public PlatformDescriptor {
        loader = Objects.requireNonNull(loader, "loader");
        minecraftVersion = Objects.requireNonNull(minecraftVersion, "minecraftVersion").trim();
        if (minecraftVersion.isEmpty() || javaVersion < 17) {
            throw new IllegalArgumentException("invalid platform descriptor");
        }
    }
}
