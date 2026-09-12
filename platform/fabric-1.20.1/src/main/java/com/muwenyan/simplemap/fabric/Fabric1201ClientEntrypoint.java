package com.muwenyan.simplemap.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;

public final class Fabric1201ClientEntrypoint implements ClientModInitializer {
    private static final KeyMapping TOGGLE_MODE = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.simplemap.toggle_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M,
                    "category.simplemap"));

    @Override
    public void onInitializeClient() {
        MapFabric1201Bootstrap bootstrap = new MapFabric1201Bootstrap();
        bootstrap.descriptor();
        bootstrap.bindWorld(new Fabric1201WorldSource(() -> Minecraft.getInstance().level));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_MODE.consumeClick()) {
                bootstrap.clientController().toggleMode();
            }
        });
    }
}
