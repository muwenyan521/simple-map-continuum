package com.muwenyan.simplemap.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = "simplemap", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class NeoForge1211ClientEntrypoint {
    private static final KeyMapping TOGGLE_MODE = new KeyMapping(
            "key.simplemap.toggle_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.simplemap");
    private static final KeyMapping OPEN_MAP = new KeyMapping(
            "key.simplemap.open_map", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "category.simplemap");
    private static final KeyMapping TOGGLE_MINIMAP = new KeyMapping(
            "key.simplemap.toggle_minimap", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.simplemap");
    private static final KeyMapping ZOOM_IN = new KeyMapping("key.simplemap.zoom_in", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_EQUAL, "category.simplemap");
    private static final KeyMapping ZOOM_OUT = new KeyMapping("key.simplemap.zoom_out", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_MINUS, "category.simplemap");
    private static final KeyMapping TOGGLE_ROTATION = new KeyMapping("key.simplemap.toggle_rotation", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.simplemap");
    private static final KeyMapping CYCLE_SHAPE = new KeyMapping("key.simplemap.cycle_shape", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.simplemap");
    private static final MapNeoForge1211Bootstrap BOOTSTRAP = new MapNeoForge1211Bootstrap();

    private NeoForge1211ClientEntrypoint() { }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        NeoForge1211Network.observe(BOOTSTRAP.clientController());
        event.register(TOGGLE_MODE);
        event.register(OPEN_MAP);
        event.register(TOGGLE_MINIMAP);
        event.register(ZOOM_IN);
        event.register(ZOOM_OUT);
        event.register(TOGGLE_ROTATION);
        event.register(CYCLE_SHAPE);
        BOOTSTRAP.bindWorld(new NeoForge1211WorldSource(() -> Minecraft.getInstance().level));
        BOOTSTRAP.clientController().bindBookStorage(Minecraft.getInstance().gameDirectory.toPath().resolve("simplemap"));
        BOOTSTRAP.clientController().bindCaveStorage(Minecraft.getInstance().gameDirectory.toPath().resolve("simplemap/caves"));
        BOOTSTRAP.clientController().bindWaypointStorage(Minecraft.getInstance().gameDirectory.toPath().resolve("simplemap"));
        BOOTSTRAP.clientController().loadConfig(Minecraft.getInstance().gameDirectory.toPath().resolve("config/simplemap.cfg"));
        BOOTSTRAP.clientController().loadMinimapConfig(Minecraft.getInstance().gameDirectory.toPath().resolve("config/simplemap-minimap.cfg"));
    }

    @EventBusSubscriber(modid = "simplemap", value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() { }

        @SubscribeEvent
        public static void clientTick(ClientTickEvent.Post event) {
            while (TOGGLE_MODE.consumeClick()) BOOTSTRAP.clientController().toggleMode();
            while (OPEN_MAP.consumeClick()) Minecraft.getInstance().setScreen(new NeoForge1211MapScreen(BOOTSTRAP));
            while (TOGGLE_MINIMAP.consumeClick()) BOOTSTRAP.clientController().toggleMinimap();
            while (ZOOM_IN.consumeClick()) BOOTSTRAP.clientController().zoomMinimap(1.25d);
            while (ZOOM_OUT.consumeClick()) BOOTSTRAP.clientController().zoomMinimap(0.8d);
            while (TOGGLE_ROTATION.consumeClick()) BOOTSTRAP.clientController().toggleMinimapRotation();
            while (CYCLE_SHAPE.consumeClick()) BOOTSTRAP.clientController().cycleMinimapShape();
        }

        @SubscribeEvent
        public static void useBook(PlayerInteractEvent.RightClickItem event) {
            var item = event.getItemStack().getItem();
            if (item == NeoForge1211Entrypoint.MAP_BOOK.get() || item == NeoForge1211Entrypoint.EMPTY_MAP_BOOK.get()) {
                Minecraft.getInstance().setScreen(new NeoForge1211MapScreen(BOOTSTRAP));
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            BOOTSTRAP.clientController().saveWaypoints();
            BOOTSTRAP.clientController().saveConfig(Minecraft.getInstance().gameDirectory.toPath().resolve("config/simplemap.cfg"));
            BOOTSTRAP.clientController().saveMinimapConfig(Minecraft.getInstance().gameDirectory.toPath().resolve("config/simplemap-minimap.cfg"));
        }

        @SubscribeEvent
        public static void renderHud(RenderGuiEvent.Post event) {
            var player = Minecraft.getInstance().player;
            if (player == null || !BOOTSTRAP.clientController().minimapEnabled()) return;
            var frame = BOOTSTRAP.clientController().buildMinimap(new com.muwenyan.simplemap.core.navigation.PlayerMapState(
                    new com.muwenyan.simplemap.core.model.DimensionId(player.level().dimension().location().toString()),
                    player.getX(), player.getZ(), player.getBlockY(), player.getYRot()),
                    BOOTSTRAP.clientController().minimapConfig(), 0);
            drawMinimap(event.getGuiGraphics(), frame);
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
}
