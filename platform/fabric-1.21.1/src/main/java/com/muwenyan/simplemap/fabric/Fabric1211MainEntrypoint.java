package com.muwenyan.simplemap.fabric;

import net.fabricmc.api.ModInitializer;

public final class Fabric1211MainEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        Fabric1211Items.register();
        new MapFabric1211Bootstrap().descriptor();
    }
}
