package com.muwenyan.simplemap.neoforge;

public final class NeoForge1211TargetTest {
    private NeoForge1211TargetTest() { }

    public static void main(String[] args) {
        MapNeoForge1211Bootstrap bootstrap = new MapNeoForge1211Bootstrap();
        if (bootstrap.descriptor().loader() != com.muwenyan.simplemap.platform.LoaderPlatform.NEOFORGE) {
            throw new AssertionError("neoforge target descriptor");
        }
        if (com.muwenyan.simplemap.platform.PlatformDiscovery.installed().size() != 1) {
            throw new AssertionError("neoforge provider discovery");
        }
        if (bootstrap.createRuntime((dimension, position) -> java.util.Optional.empty(), frame -> { },
                com.muwenyan.simplemap.core.config.MapConfig.defaults()).lifecycle().state()
                != com.muwenyan.simplemap.core.session.RuntimeLifecycle.READY) {
            throw new AssertionError("runtime lifecycle");
        }
        System.out.println("NEOFORGE_1211_TARGET_PASS");
    }
}
