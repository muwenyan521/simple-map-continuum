package com.muwenyan.simplemap.platform;

import java.util.List;

public final class SupportedTargets {
    private SupportedTargets() { }

    public static List<PlatformDescriptor> current() {
        return List.of(
                new PlatformDescriptor(LoaderPlatform.FABRIC, "1.20.1", 17),
                new PlatformDescriptor(LoaderPlatform.FORGE, "1.20.1", 17),
                new PlatformDescriptor(LoaderPlatform.FABRIC, "1.21.1", 21),
                new PlatformDescriptor(LoaderPlatform.NEOFORGE, "1.21.1", 21));
    }
}
