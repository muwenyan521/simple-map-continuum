package com.muwenyan.simplemap.forge;

import com.muwenyan.simplemap.platform.LoaderPlatform;
import com.muwenyan.simplemap.platform.PlatformBootstrap;
import com.muwenyan.simplemap.platform.PlatformDescriptor;

public final class MapForge1201Bootstrap implements PlatformBootstrap {
    private static final PlatformDescriptor DESCRIPTOR = new PlatformDescriptor(LoaderPlatform.FORGE, "1.20.1", 17);

    @Override
    public PlatformDescriptor descriptor() {
        return DESCRIPTOR;
    }
}
