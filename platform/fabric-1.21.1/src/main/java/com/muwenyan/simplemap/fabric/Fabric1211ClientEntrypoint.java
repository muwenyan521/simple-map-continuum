package com.muwenyan.simplemap.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResultHolder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public final class Fabric1211ClientEntrypoint implements ClientModInitializer {
    private static final KeyMapping TOGGLE_MODE = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.simplemap.toggle_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M,
                    "category.simplemap"));
    private static final KeyMapping OPEN_MAP = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.simplemap.open_map", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "category.simplemap"));
    private static final KeyMapping TOGGLE_MINIMAP = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.simplemap.toggle_minimap", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.simplemap"));
    private static final KeyMapping ZOOM_IN = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.simplemap.zoom_in", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, "category.simplemap"));
    private static final KeyMapping ZOOM_OUT = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.simplemap.zoom_out", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, "category.simplemap"));
    private static final KeyMapping TOGGLE_ROTATION = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.simplemap.toggle_rotation", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.simplemap"));
    private static final KeyMapping CYCLE_SHAPE = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.simplemap.cycle_shape", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.simplemap"));
    private static final KeyMapping TOGGLE_COORDS = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.simplemap.toggle_coordinates", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.simplemap"));
    private static final KeyMapping CYCLE_ANCHOR = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.simplemap.cycle_anchor", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, "category.simplemap"));

    @Override
    public void onInitializeClient() {
        MapFabric1211Bootstrap bootstrap = new MapFabric1211Bootstrap();
        Fabric1211Network.registerClient();
        Fabric1211Network.observe(bootstrap.clientController());
        bootstrap.descriptor();
        bootstrap.bindWorld(new Fabric1211WorldSource(() -> Minecraft.getInstance().level));
        bootstrap.clientController().bindBookStorage(Minecraft.getInstance().gameDirectory.toPath().resolve("simplemap"));
        bootstrap.clientController().bindCaveStorage(Minecraft.getInstance().gameDirectory.toPath().resolve("simplemap/caves"));
        bootstrap.clientController().bindWaypointStorage(Minecraft.getInstance().gameDirectory.toPath().resolve("simplemap"));
        bootstrap.clientController().loadConfig(Minecraft.getInstance().gameDirectory.toPath().resolve("config/simplemap.cfg"));
        bootstrap.clientController().loadMinimapConfig(Minecraft.getInstance().gameDirectory.toPath().resolve("config/simplemap-minimap.cfg"));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> save(bootstrap, client));
        UseItemCallback.EVENT.register((player, level, hand) -> {
            var item = player.getItemInHand(hand).getItem();
            if (item == Fabric1211Items.EMPTY_MAP_BOOK || item == Fabric1211Items.MAP_BOOK) {
                Minecraft.getInstance().setScreen(new Fabric1211MapScreen(bootstrap));
                return InteractionResultHolder.success(player.getItemInHand(hand));
            }
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_MODE.consumeClick()) {
                bootstrap.clientController().toggleMode();
            }
            while (OPEN_MAP.consumeClick()) client.setScreen(new Fabric1211MapScreen(bootstrap));
            while (TOGGLE_MINIMAP.consumeClick()) bootstrap.clientController().toggleMinimap();
            while (ZOOM_IN.consumeClick()) bootstrap.clientController().zoomMinimap(1.25d);
            while (ZOOM_OUT.consumeClick()) bootstrap.clientController().zoomMinimap(0.8d);
            while (TOGGLE_ROTATION.consumeClick()) bootstrap.clientController().toggleMinimapRotation();
            while (CYCLE_SHAPE.consumeClick()) bootstrap.clientController().cycleMinimapShape();
            while (TOGGLE_COORDS.consumeClick()) bootstrap.clientController().toggleMinimapCoordinates();
            while (CYCLE_ANCHOR.consumeClick()) bootstrap.clientController().cycleMinimapAnchor();
        });
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            if (Minecraft.getInstance().player == null || !bootstrap.clientController().minimapEnabled()) return;
            var player = Minecraft.getInstance().player;
            var frame = bootstrap.clientController().buildMinimap(new com.muwenyan.simplemap.core.navigation.PlayerMapState(
                    new com.muwenyan.simplemap.core.model.DimensionId(player.level().dimension().location().toString()),
                    player.getX(), player.getZ(), player.getBlockY(), player.getYRot()),
                    bootstrap.clientController().minimapConfig(), 0);
            drawMinimap(graphics, frame);
        });
    }

    private static void save(MapFabric1211Bootstrap bootstrap, Minecraft client) {
        java.nio.file.Path root = client.gameDirectory.toPath();
        bootstrap.clientController().saveWaypoints();
        bootstrap.clientController().saveConfig(root.resolve("config/simplemap.cfg"));
        bootstrap.clientController().saveMinimapConfig(root.resolve("config/simplemap-minimap.cfg"));
    }

    private static void drawMinimap(net.minecraft.client.gui.GuiGraphics graphics,
                                    com.muwenyan.simplemap.core.minimap.MinimapFrame frame) {
        var config = frame.config();
        int size = config.sizePixels();
        var layout = com.muwenyan.simplemap.core.minimap.MinimapLayout.place(config,
                Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                Minecraft.getInstance().getWindow().getGuiScaledHeight(), 4);
        int left = layout.x();
        int top = layout.y();
        graphics.fill(left - 2, top - 2, left + size + 2, top + size + 2, 0xA0000000);
        for (var tile : frame.tiles()) {
            var point = com.muwenyan.simplemap.core.minimap.MinimapProjection.chunkCenterToScreen(tile.chunk(), frame.playerX(), frame.playerZ(), config.zoom(), frame.playerYaw(), config.rotateWithPlayer(), size);
            if (!com.muwenyan.simplemap.core.minimap.MinimapProjection.visible(point, size, config.shape())) continue;
            int x = left + (int) Math.floor(point.x()) - 2;
            int y = top + (int) Math.floor(point.z()) - 2;
            graphics.fill(x, y, x + 4, y + 4, tile.argb());
        }
        var markerPlayer = Minecraft.getInstance().player;
        if (markerPlayer != null) {
            graphics.fill(left + size / 2 - 2, top + size / 2 - 2, left + size / 2 + 3, top + size / 2 + 3, 0xFFFFFFFF);
        }
        if (config.showCoordinates() && Minecraft.getInstance().player != null) {
            var player = Minecraft.getInstance().player;
            graphics.drawString(Minecraft.getInstance().font, player.getBlockX() + ", " + player.getBlockZ(), left + 2, top + size - 10, 0xFFFFFFFF, true);
        }
        graphics.drawString(Minecraft.getInstance().font, "N", left + size / 2 - 3, top + 2, 0xFFFF5555, true);
    }
}
