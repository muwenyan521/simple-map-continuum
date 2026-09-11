package com.muwenyan.simplemap.fabric;

import com.muwenyan.simplemap.platform.LoaderPlatform;
import com.muwenyan.simplemap.platform.PlatformBootstrap;
import com.muwenyan.simplemap.platform.PlatformDescriptor;

public final class MapFabric1211Bootstrap implements PlatformBootstrap {
    private static final PlatformDescriptor DESCRIPTOR = new PlatformDescriptor(LoaderPlatform.FABRIC, "1.21.1", 21);

    @Override
    public PlatformDescriptor descriptor() {
        return DESCRIPTOR;
    }
}
