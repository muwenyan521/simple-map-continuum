package com.muwenyan.simplemap.neoforge;

import com.muwenyan.simplemap.platform.LoaderPlatform;
import com.muwenyan.simplemap.platform.PlatformBootstrap;
import com.muwenyan.simplemap.platform.PlatformDescriptor;

public final class MapNeoForge1211Bootstrap implements PlatformBootstrap {
    private static final PlatformDescriptor DESCRIPTOR = new PlatformDescriptor(LoaderPlatform.NEOFORGE, "1.21.1", 21);

    @Override
    public PlatformDescriptor descriptor() {
        return DESCRIPTOR;
    }
}
