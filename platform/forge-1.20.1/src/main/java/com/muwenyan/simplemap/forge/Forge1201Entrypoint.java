package com.muwenyan.simplemap.forge;

import net.minecraftforge.fml.common.Mod;

@Mod("simplemap")
public final class Forge1201Entrypoint {
    public Forge1201Entrypoint() {
        new MapForge1201Bootstrap().descriptor();
    }
}
