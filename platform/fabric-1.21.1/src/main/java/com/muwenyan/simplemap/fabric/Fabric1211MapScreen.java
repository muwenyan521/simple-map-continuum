package com.muwenyan.simplemap.fabric;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.map.MapCell;

public final class Fabric1211MapScreen extends Screen {
    private final MapFabric1211Bootstrap bootstrap;

    public Fabric1211MapScreen(MapFabric1211Bootstrap bootstrap) {
        super(Component.translatable("screen.simplemap.map"));
        this.bootstrap = bootstrap;
        if (Minecraft.getInstance().player != null) {
            var player = Minecraft.getInstance().player;
            var dimension = new DimensionId(player.level().dimension().location().toString());
            var center = new ChunkPos(player.chunkPosition().x, player.chunkPosition().z);
            if (bootstrap.clientController().mode() == com.muwenyan.simplemap.core.model.MapMode.CAVE)
                bootstrap.clientController().refreshCave(dimension, center, 4, com.muwenyan.simplemap.core.cave.CaveConfig.defaults());
            else bootstrap.clientController().refreshSurface(dimension, center, 4);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("screen.simplemap.mode", bootstrap.clientController().mode().name()),
                width / 2, height / 2 - 10, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("screen.simplemap.cells",
                bootstrap.clientController().runtime().currentRegion() == null ? 0
                        : bootstrap.clientController().runtime().currentRegion().completedCells().size()),
                width / 2, height / 2 + 10, 0xFFB0D0B0);
        var region = bootstrap.clientController().runtime().currentRegion();
        if (region != null) {
            int size = Math.min(12, Math.max(1, Math.min(width, height) / 40));
            int left = width / 2 - 16 * size;
            int top = height / 2 - 16 * size;
            for (int z = 0; z < 32; z++) for (int x = 0; x < 32; x++) {
                MapCell cell = region.cell(x, z);
                graphics.fill(left + x * size, top + z * size, left + (x + 1) * size, top + (z + 1) * size,
                        cell == null ? 0x40202020 : cell.colorArgb());
            }
            if (Minecraft.getInstance().player != null) {
                var player = Minecraft.getInstance().player;
                int px = left + (player.chunkPosition().x - region.origin().x()) * size;
                int pz = top + (player.chunkPosition().z - region.origin().z()) * size;
                graphics.fill(px - 2, pz - 2, px + 3, pz + 3, 0xFFFFFFFF);
                var dimension = new DimensionId(player.level().dimension().location().toString());
                for (var waypoint : bootstrap.clientController().visibleWaypoints(dimension)) {
                    int wx = left + ((waypoint.position().x() >> 4) - region.origin().x()) * size;
                    int wz = top + ((waypoint.position().z() >> 4) - region.origin().z()) * size;
                    graphics.fill(wx - 2, wz - 2, wx + 3, wz + 3, 0xFFFFD040);
                }
            }
        }
        graphics.drawCenteredString(font, Component.translatable("screen.simplemap.close"), width / 2, height - 30, 0xFFAAAAAA);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
