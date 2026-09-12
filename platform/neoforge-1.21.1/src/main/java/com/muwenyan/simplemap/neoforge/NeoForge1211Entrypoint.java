package com.muwenyan.simplemap.neoforge;

import net.neoforged.fml.common.Mod;

@Mod("simplemap")
public final class NeoForge1211Entrypoint {
    public NeoForge1211Entrypoint() {
        new MapNeoForge1211Bootstrap().descriptor();
    }
}
