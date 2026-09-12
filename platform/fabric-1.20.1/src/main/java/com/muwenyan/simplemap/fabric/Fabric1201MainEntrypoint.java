package com.muwenyan.simplemap.fabric;

import net.fabricmc.api.ModInitializer;

public final class Fabric1201MainEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        Fabric1201Items.register();
        new MapFabric1201Bootstrap().descriptor();
    }
}
