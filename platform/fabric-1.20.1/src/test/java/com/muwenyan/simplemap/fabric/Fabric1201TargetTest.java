package com.muwenyan.simplemap.fabric;

public final class Fabric1201TargetTest {
    private Fabric1201TargetTest() { }

    public static void main(String[] args) {
        MapFabric1201Bootstrap bootstrap = new MapFabric1201Bootstrap();
        if (!"1.20.1".equals(bootstrap.descriptor().minecraftVersion())) {
            throw new AssertionError("fabric target descriptor");
        }
        if (com.muwenyan.simplemap.platform.PlatformDiscovery.installed().size() != 1) {
            throw new AssertionError("fabric provider discovery");
        }
        if (bootstrap.createRuntime((dimension, position) -> java.util.Optional.empty(), frame -> { },
                com.muwenyan.simplemap.core.config.MapConfig.defaults()).lifecycle().state()
                != com.muwenyan.simplemap.core.session.RuntimeLifecycle.READY) {
            throw new AssertionError("runtime lifecycle");
        }
        System.out.println("FABRIC_1201_TARGET_PASS");
    }
}
