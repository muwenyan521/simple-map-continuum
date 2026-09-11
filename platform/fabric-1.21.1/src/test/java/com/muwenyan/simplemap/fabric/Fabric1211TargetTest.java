package com.muwenyan.simplemap.fabric;

public final class Fabric1211TargetTest {
    private Fabric1211TargetTest() { }

    public static void main(String[] args) {
        MapFabric1211Bootstrap bootstrap = new MapFabric1211Bootstrap();
        if (bootstrap.descriptor().javaVersion() != 21) {
            throw new AssertionError("fabric target descriptor");
        }
        System.out.println("FABRIC_1211_TARGET_PASS");
    }
}
