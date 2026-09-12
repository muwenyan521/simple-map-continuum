package com.muwenyan.simplemap.forge;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;

@Mod("simplemap")
public final class Forge1201Entrypoint {
    private final MapForge1201Bootstrap bootstrap = new MapForge1201Bootstrap();
    private static final KeyMapping TOGGLE_MODE = new KeyMapping(
            "key.simplemap.toggle_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M,
            "category.simplemap");

    public Forge1201Entrypoint() {
        bootstrap.descriptor();
        bootstrap.bindWorld(new Forge1201WorldSource(() -> Minecraft.getInstance().level));
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_MODE);
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        while (TOGGLE_MODE.consumeClick()) {
            bootstrap.clientController().toggleMode();
        }
    }
}
