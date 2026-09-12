package com.muwenyan.simplemap.forge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

@Mod.EventBusSubscriber(modid = "simplemap", value = net.minecraftforge.api.distmarker.Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Forge1201ClientEntrypoint {
    private static final KeyMapping TOGGLE_MODE = new KeyMapping(
            "key.simplemap.toggle_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.simplemap");
    private static final KeyMapping OPEN_MAP = new KeyMapping(
            "key.simplemap.open_map", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "category.simplemap");
    private static final KeyMapping TOGGLE_MINIMAP = new KeyMapping(
            "key.simplemap.toggle_minimap", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.simplemap");
    private static final MapForge1201Bootstrap BOOTSTRAP = new MapForge1201Bootstrap();

    private Forge1201ClientEntrypoint() { }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_MODE);
        event.register(OPEN_MAP);
        event.register(TOGGLE_MINIMAP);
        BOOTSTRAP.bindWorld(new Forge1201WorldSource(() -> Minecraft.getInstance().level));
        BOOTSTRAP.clientController().bindBookStorage(Minecraft.getInstance().gameDirectory.toPath().resolve("simplemap"));
        BOOTSTRAP.clientController().bindCaveStorage(Minecraft.getInstance().gameDirectory.toPath().resolve("simplemap/caves"));
        BOOTSTRAP.clientController().bindWaypointStorage(Minecraft.getInstance().gameDirectory.toPath().resolve("simplemap"));
        BOOTSTRAP.clientController().loadConfig(Minecraft.getInstance().gameDirectory.toPath().resolve("config/simplemap.cfg"));
    }

    @Mod.EventBusSubscriber(modid = "simplemap", value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() { }

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            while (TOGGLE_MODE.consumeClick()) BOOTSTRAP.clientController().toggleMode();
            while (OPEN_MAP.consumeClick()) Minecraft.getInstance().setScreen(new Forge1201MapScreen(BOOTSTRAP));
            while (TOGGLE_MINIMAP.consumeClick()) BOOTSTRAP.clientController().toggleMinimap();
        }

        @SubscribeEvent
        public static void useBook(PlayerInteractEvent.RightClickItem event) {
            var item = event.getItemStack().getItem();
            if (item == Forge1201Entrypoint.MAP_BOOK.get() || item == Forge1201Entrypoint.EMPTY_MAP_BOOK.get()) {
                Minecraft.getInstance().setScreen(new Forge1201MapScreen(BOOTSTRAP));
                event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            BOOTSTRAP.clientController().saveWaypoints();
            BOOTSTRAP.clientController().saveConfig(Minecraft.getInstance().gameDirectory.toPath().resolve("config/simplemap.cfg"));
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
}
