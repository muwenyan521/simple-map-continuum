package com.muwenyan.simplemap.forge;

public final class Forge1201TargetTest {
    private Forge1201TargetTest() { }

    public static void main(String[] args) {
        MapForge1201Bootstrap bootstrap = new MapForge1201Bootstrap();
        if (bootstrap.descriptor().loader().name().isEmpty()) {
            throw new AssertionError("forge target descriptor");
        }
        System.out.println("FORGE_1201_TARGET_PASS");
    }
}
