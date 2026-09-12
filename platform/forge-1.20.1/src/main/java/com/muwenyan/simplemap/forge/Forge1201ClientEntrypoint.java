package com.muwenyan.simplemap.forge;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "simplemap", value = net.minecraftforge.api.distmarker.Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Forge1201ClientEntrypoint {
    private static final KeyMapping TOGGLE_MODE = new KeyMapping(
            "key.simplemap.toggle_mode", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.simplemap");
    private static final MapForge1201Bootstrap BOOTSTRAP = new MapForge1201Bootstrap();

    private Forge1201ClientEntrypoint() { }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_MODE);
        BOOTSTRAP.bindWorld(new Forge1201WorldSource(() -> Minecraft.getInstance().level));
    }

    @Mod.EventBusSubscriber(modid = "simplemap", value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() { }

        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            while (TOGGLE_MODE.consumeClick()) BOOTSTRAP.clientController().toggleMode();
        }
    }
}
