package com.muwenyan.simplemap.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public final class Fabric1211ClientEntrypoint implements ClientModInitializer {
    private static final KeyMapping TOGGLE_MODE = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.simplemap.toggle_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M,
                    "category.simplemap"));
    private static final KeyMapping OPEN_MAP = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.simplemap.open_map", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "category.simplemap"));

    @Override
    public void onInitializeClient() {
        MapFabric1211Bootstrap bootstrap = new MapFabric1211Bootstrap();
        bootstrap.descriptor();
        bootstrap.bindWorld(new Fabric1211WorldSource(() -> Minecraft.getInstance().level));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_MODE.consumeClick()) {
                bootstrap.clientController().toggleMode();
            }
            while (OPEN_MAP.consumeClick()) client.setScreen(new Fabric1211MapScreen(bootstrap));
        });
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            if (Minecraft.getInstance().player == null) return;
            var player = Minecraft.getInstance().player;
            var frame = bootstrap.clientController().buildMinimap(new com.muwenyan.simplemap.core.navigation.PlayerMapState(
                    new com.muwenyan.simplemap.core.model.DimensionId(player.level().dimension().location().toString()),
                    player.getX(), player.getZ(), player.getBlockY(), player.getYRot()),
                    com.muwenyan.simplemap.core.minimap.MinimapConfig.defaults(), 0);
            drawMinimap(graphics, frame);
        });
    }

    private static void drawMinimap(net.minecraft.client.gui.GuiGraphics graphics,
                                    com.muwenyan.simplemap.core.minimap.MinimapFrame frame) {
        var config = frame.config();
        int size = config.sizePixels();
        int left = 4;
        int top = 4;
        graphics.fill(left - 2, top - 2, left + size + 2, top + size + 2, 0xA0000000);
        for (var tile : frame.tiles()) {
            var point = com.muwenyan.simplemap.core.minimap.MinimapProjection.chunkCenterToScreen(tile.chunk(), frame.playerX(), frame.playerZ(), config.zoom(), frame.playerYaw(), config.rotateWithPlayer(), size);
            if (!com.muwenyan.simplemap.core.minimap.MinimapProjection.visible(point, size, config.shape())) continue;
            int x = left + (int) Math.floor(point.x()) - 2;
            int y = top + (int) Math.floor(point.z()) - 2;
            graphics.fill(x, y, x + 4, y + 4, tile.argb());
        }
    }
}
