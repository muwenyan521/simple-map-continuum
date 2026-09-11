package com.muwenyan.simplemap.fabric;

public final class Fabric1201TargetTest {
    private Fabric1201TargetTest() { }

    public static void main(String[] args) {
        MapFabric1201Bootstrap bootstrap = new MapFabric1201Bootstrap();
        if (!"1.20.1".equals(bootstrap.descriptor().minecraftVersion())) {
            throw new AssertionError("fabric target descriptor");
        }
        System.out.println("FABRIC_1201_TARGET_PASS");
    }
}
