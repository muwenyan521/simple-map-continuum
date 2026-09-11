package com.muwenyan.simplemap.fabric;

import net.fabricmc.api.ClientModInitializer;

public final class Fabric1211ClientEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        new MapFabric1211Bootstrap().descriptor();
    }
}
