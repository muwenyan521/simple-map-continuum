package com.muwenyan.simplemap.neoforge;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@Mod("simplemap")
public final class NeoForge1211Entrypoint {
    private final MapNeoForge1211Bootstrap bootstrap = new MapNeoForge1211Bootstrap();
    private static final KeyMapping TOGGLE_MODE = new KeyMapping(
            "key.simplemap.toggle_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M,
            "category.simplemap");

    public NeoForge1211Entrypoint() {
        bootstrap.descriptor();
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_MODE);
    }

    @SubscribeEvent
    public void clientTick(ClientTickEvent.Post event) {
        while (TOGGLE_MODE.consumeClick()) {
            bootstrap.clientController().toggleMode();
        }
    }
}
