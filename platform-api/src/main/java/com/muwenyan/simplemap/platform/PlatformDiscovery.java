package com.muwenyan.simplemap.platform;

import java.util.List;
import java.util.ServiceLoader;

public final class PlatformDiscovery {
    private PlatformDiscovery() {
    }

    public static List<PlatformBootstrap> installed() {
        return ServiceLoader.load(PlatformBootstrap.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
    }
}
