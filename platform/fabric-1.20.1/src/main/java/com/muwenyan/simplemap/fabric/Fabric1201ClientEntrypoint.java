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

public final class Fabric1201ClientEntrypoint implements ClientModInitializer {
    private static final KeyMapping TOGGLE_MODE = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.simplemap.toggle_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M,
                    "category.simplemap"));
    private static final KeyMapping OPEN_MAP = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.simplemap.open_map", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "category.simplemap"));
    private static final KeyMapping TOGGLE_MINIMAP = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.simplemap.toggle_minimap", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.simplemap"));

    @Override
    public void onInitializeClient() {
        MapFabric1201Bootstrap bootstrap = new MapFabric1201Bootstrap();
        bootstrap.descriptor();
        bootstrap.bindWorld(new Fabric1201WorldSource(() -> Minecraft.getInstance().level));
        bootstrap.clientController().bindBookStorage(Minecraft.getInstance().gameDirectory.toPath().resolve("simplemap"));
        bootstrap.clientController().bindWaypointStorage(Minecraft.getInstance().gameDirectory.toPath().resolve("simplemap"));
        bootstrap.clientController().loadConfig(Minecraft.getInstance().gameDirectory.toPath().resolve("config/simplemap.cfg"));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> save(bootstrap, client));
        UseItemCallback.EVENT.register((player, level, hand) -> {
            var item = player.getItemInHand(hand).getItem();
            if (item == Fabric1201Items.EMPTY_MAP_BOOK || item == Fabric1201Items.MAP_BOOK) {
                Minecraft.getInstance().setScreen(new Fabric1201MapScreen(bootstrap));
                return InteractionResultHolder.success(player.getItemInHand(hand));
            }
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_MODE.consumeClick()) {
                bootstrap.clientController().toggleMode();
            }
            while (OPEN_MAP.consumeClick()) client.setScreen(new Fabric1201MapScreen(bootstrap));
            while (TOGGLE_MINIMAP.consumeClick()) bootstrap.clientController().toggleMinimap();
        });
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            if (Minecraft.getInstance().player == null || !bootstrap.clientController().minimapEnabled()) return;
            var controller = bootstrap.clientController();
            var player = Minecraft.getInstance().player;
            var frame = controller.buildMinimap(new com.muwenyan.simplemap.core.navigation.PlayerMapState(
                    new com.muwenyan.simplemap.core.model.DimensionId(player.level().dimension().location().toString()),
                    player.getX(), player.getZ(), player.getBlockY(), player.getYRot()),
                    com.muwenyan.simplemap.core.minimap.MinimapConfig.defaults(), 0);
            drawMinimap(graphics, frame);
        });
    }

    private static void save(MapFabric1201Bootstrap bootstrap, Minecraft client) {
        java.nio.file.Path root = client.gameDirectory.toPath();
        bootstrap.clientController().saveWaypoints();
        bootstrap.clientController().saveConfig(root.resolve("config/simplemap.cfg"));
    }

    private static void drawMinimap(net.minecraft.client.gui.GuiGraphics graphics,
                                    com.muwenyan.simplemap.core.minimap.MinimapFrame frame) {
        var config = frame.config();
        int size = config.sizePixels();
        int left = 4;
        int top = 4;
        graphics.fill(left - 2, top - 2, left + size + 2, top + size + 2, 0xA0000000);
        for (var tile : frame.tiles()) {
            var point = com.muwenyan.simplemap.core.minimap.MinimapProjection.chunkCenterToScreen(tile.chunk(),
                    frame.playerX(), frame.playerZ(), config.zoom(), frame.playerYaw(), config.rotateWithPlayer(), size);
            if (!com.muwenyan.simplemap.core.minimap.MinimapProjection.visible(point, size, config.shape())) continue;
            int x = left + (int) Math.floor(point.x()) - 2;
            int y = top + (int) Math.floor(point.z()) - 2;
            graphics.fill(x, y, x + 4, y + 4, tile.argb());
        }
    }
}
