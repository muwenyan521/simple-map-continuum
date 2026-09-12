package com.muwenyan.simplemap.fabric;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class Fabric1211MapScreen extends Screen {
    private final MapFabric1211Bootstrap bootstrap;

    public Fabric1211MapScreen(MapFabric1211Bootstrap bootstrap) {
        super(Component.translatable("screen.simplemap.map"));
        this.bootstrap = bootstrap;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("screen.simplemap.mode", bootstrap.clientController().mode().name()),
                width / 2, height / 2 - 10, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("screen.simplemap.close"), width / 2, height - 30, 0xFFAAAAAA);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
