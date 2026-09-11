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
        System.out.println("NEOFORGE_1211_TARGET_PASS");
    }
}
