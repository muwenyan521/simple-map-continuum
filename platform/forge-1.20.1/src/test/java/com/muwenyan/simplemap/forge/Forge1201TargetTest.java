package com.muwenyan.simplemap.forge;

public final class Forge1201TargetTest {
    private Forge1201TargetTest() { }

    public static void main(String[] args) {
        MapForge1201Bootstrap bootstrap = new MapForge1201Bootstrap();
        if (bootstrap.descriptor().loader().name().isEmpty()) {
            throw new AssertionError("forge target descriptor");
        }
        if (com.muwenyan.simplemap.platform.PlatformDiscovery.installed().size() != 1) {
            throw new AssertionError("forge provider discovery");
        }
        if (bootstrap.createRuntime((dimension, position) -> java.util.Optional.empty(), frame -> { },
                com.muwenyan.simplemap.core.config.MapConfig.defaults()).lifecycle().state()
                != com.muwenyan.simplemap.core.session.RuntimeLifecycle.READY) {
            throw new AssertionError("runtime lifecycle");
        }
        System.out.println("FORGE_1201_TARGET_PASS");
    }
}
