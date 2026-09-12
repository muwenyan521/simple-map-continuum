package com.muwenyan.simplemap.neoforge;

import com.muwenyan.simplemap.platform.LoaderPlatform;
import com.muwenyan.simplemap.platform.PlatformBootstrap;
import com.muwenyan.simplemap.platform.PlatformDescriptor;
import com.muwenyan.simplemap.platform.MapClientController;

public final class MapNeoForge1211Bootstrap implements PlatformBootstrap {
    private static final PlatformDescriptor DESCRIPTOR = new PlatformDescriptor(LoaderPlatform.NEOFORGE, "1.21.1", 21);
    private static final MapClientController CLIENT = new MapClientController();

    @Override
    public PlatformDescriptor descriptor() {
        return DESCRIPTOR;
    }

    public MapClientController clientController() {
        return CLIENT;
    }
}
