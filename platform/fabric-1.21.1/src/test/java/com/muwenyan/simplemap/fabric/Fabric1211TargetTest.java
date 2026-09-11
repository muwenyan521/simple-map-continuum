package com.muwenyan.simplemap.fabric;

public final class Fabric1211TargetTest {
    private Fabric1211TargetTest() { }

    public static void main(String[] args) {
        MapFabric1211Bootstrap bootstrap = new MapFabric1211Bootstrap();
        if (bootstrap.descriptor().javaVersion() != 21) {
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
        System.out.println("FABRIC_1211_TARGET_PASS");
    }
}
