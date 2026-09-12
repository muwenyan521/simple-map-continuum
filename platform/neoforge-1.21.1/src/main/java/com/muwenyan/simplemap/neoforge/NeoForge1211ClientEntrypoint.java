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

@SuppressWarnings("removal")
@EventBusSubscriber(modid = "simplemap", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class NeoForge1211ClientEntrypoint {
    private static final KeyMapping TOGGLE_MODE = new KeyMapping(
            "key.simplemap.toggle_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.simplemap");
    private static final KeyMapping OPEN_MAP = new KeyMapping(
            "key.simplemap.open_map", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "category.simplemap");
    private static final MapNeoForge1211Bootstrap BOOTSTRAP = new MapNeoForge1211Bootstrap();

    private NeoForge1211ClientEntrypoint() { }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_MODE);
        event.register(OPEN_MAP);
        BOOTSTRAP.bindWorld(new NeoForge1211WorldSource(() -> Minecraft.getInstance().level));
    }

    @EventBusSubscriber(modid = "simplemap", value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() { }

        @SubscribeEvent
        public static void clientTick(ClientTickEvent.Post event) {
            while (TOGGLE_MODE.consumeClick()) BOOTSTRAP.clientController().toggleMode();
            while (OPEN_MAP.consumeClick()) Minecraft.getInstance().setScreen(new NeoForge1211MapScreen(BOOTSTRAP));
        }

        @SubscribeEvent
        public static void renderHud(RenderGuiEvent.Post event) {
            var player = Minecraft.getInstance().player;
            if (player == null) return;
            var frame = BOOTSTRAP.clientController().buildMinimap(new com.muwenyan.simplemap.core.navigation.PlayerMapState(
                    new com.muwenyan.simplemap.core.model.DimensionId(player.level().dimension().location().toString()),
                    player.getX(), player.getZ(), player.getBlockY(), player.getYRot()),
                    com.muwenyan.simplemap.core.minimap.MinimapConfig.defaults(), 0);
            event.getGuiGraphics().drawString(Minecraft.getInstance().font, "Map " + frame.tiles().size(), 4, 4, 0xFFFFFFFF, true);
        }
    }
}
