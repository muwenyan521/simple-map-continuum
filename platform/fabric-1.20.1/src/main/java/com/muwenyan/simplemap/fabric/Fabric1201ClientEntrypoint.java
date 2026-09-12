package com.muwenyan.simplemap.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class Fabric1201ClientEntrypoint implements ClientModInitializer {
    private static final KeyMapping TOGGLE_MODE = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.simplemap.toggle_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M,
                    "category.simplemap"));
    private static final KeyMapping OPEN_MAP = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.simplemap.open_map", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "category.simplemap"));

    @Override
    public void onInitializeClient() {
        MapFabric1201Bootstrap bootstrap = new MapFabric1201Bootstrap();
        bootstrap.descriptor();
        bootstrap.bindWorld(new Fabric1201WorldSource(() -> Minecraft.getInstance().level));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_MODE.consumeClick()) {
                bootstrap.clientController().toggleMode();
            }
            while (OPEN_MAP.consumeClick()) client.setScreen(new Fabric1201MapScreen(bootstrap));
        });
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            if (Minecraft.getInstance().player == null) return;
            var controller = bootstrap.clientController();
            var player = Minecraft.getInstance().player;
            var frame = controller.buildMinimap(new com.muwenyan.simplemap.core.navigation.PlayerMapState(
                    new com.muwenyan.simplemap.core.model.DimensionId(player.level().dimension().location().toString()),
                    player.getX(), player.getZ(), player.getBlockY(), player.getYRot()),
                    com.muwenyan.simplemap.core.minimap.MinimapConfig.defaults(), 0);
            graphics.drawString(Minecraft.getInstance().font, "Map " + frame.tiles().size(), 4, 4, 0xFFFFFFFF, true);
        });
    }
}
