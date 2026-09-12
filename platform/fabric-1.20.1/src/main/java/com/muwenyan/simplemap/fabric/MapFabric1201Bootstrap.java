package com.muwenyan.simplemap.fabric;

import com.muwenyan.simplemap.platform.LoaderPlatform;
import com.muwenyan.simplemap.platform.PlatformBootstrap;
import com.muwenyan.simplemap.platform.PlatformDescriptor;
import com.muwenyan.simplemap.platform.MapClientController;
import com.muwenyan.simplemap.platform.port.WorldSourcePort;

public final class MapFabric1201Bootstrap implements PlatformBootstrap {
    private static final PlatformDescriptor DESCRIPTOR = new PlatformDescriptor(LoaderPlatform.FABRIC, "1.20.1", 17);
    private static MapClientController client = new MapClientController();

    @Override
    public PlatformDescriptor descriptor() {
        return DESCRIPTOR;
    }

    public MapClientController clientController() {
        return client;
    }

    public synchronized void bindWorld(WorldSourcePort world) {
        client = new MapClientController(world);
    }
}
