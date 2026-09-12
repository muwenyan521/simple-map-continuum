package com.muwenyan.simplemap.fabric;

import net.fabricmc.api.ModInitializer;

public final class Fabric1201MainEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        new MapFabric1201Bootstrap().descriptor();
    }
}
