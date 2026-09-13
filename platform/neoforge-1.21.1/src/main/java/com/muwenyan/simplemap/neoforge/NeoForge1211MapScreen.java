package com.muwenyan.simplemap.neoforge;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.map.MapCell;

public final class NeoForge1211MapScreen extends Screen {
    private final MapNeoForge1211Bootstrap bootstrap;

    public NeoForge1211MapScreen(MapNeoForge1211Bootstrap bootstrap) {
        super(Component.translatable("screen.simplemap.map"));
        this.bootstrap = bootstrap;
        if (Minecraft.getInstance().player != null) {
            var player = Minecraft.getInstance().player;
            var dimension = new DimensionId(player.level().dimension().location().toString());
            var center = new ChunkPos(player.chunkPosition().x, player.chunkPosition().z);
            bootstrap.clientController().runtime().navigation().center(center.x(), center.z());
            if (bootstrap.clientController().mode() == com.muwenyan.simplemap.core.model.MapMode.CAVE)
                bootstrap.clientController().restoreCave(dimension, 0, Math.floorDiv(center.x(), 32), Math.floorDiv(center.z(), 32));
            if (bootstrap.clientController().mode() == com.muwenyan.simplemap.core.model.MapMode.CAVE)
                bootstrap.clientController().refreshCave(dimension, center, 4, com.muwenyan.simplemap.core.cave.CaveConfig.defaults());
            else {
                bootstrap.clientController().restoreSurface(dimension, Math.floorDiv(center.x(), 32), Math.floorDiv(center.z(), 32));
                bootstrap.clientController().refreshSurface(dimension, center, 4);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("screen.simplemap.mode", bootstrap.clientController().mode().name()), width / 2, height / 2 - 10, 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.translatable("screen.simplemap.cells",
                bootstrap.clientController().runtime().currentRegion() == null ? 0
                        : bootstrap.clientController().runtime().currentRegion().completedCells().size()), width / 2, height / 2 + 10, 0xFFB0D0B0);
        if (Minecraft.getInstance().player != null) {
            var playerInfo = Minecraft.getInstance().player;
            var info = bootstrap.clientController().runtime().world().inspect(new DimensionId(playerInfo.level().dimension().location().toString()),
                    new com.muwenyan.simplemap.core.model.BlockPos(playerInfo.getBlockX(), playerInfo.getBlockY(), playerInfo.getBlockZ()));
            info.block().ifPresent(block -> graphics.drawCenteredString(font, block.displayName(), width / 2, height / 2 + 30, block.colorArgb()));
        }
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
            if (bootstrap.clientController().mode() == com.muwenyan.simplemap.core.model.MapMode.CAVE) {
                var caveDimension = new DimensionId(Minecraft.getInstance().player.level().dimension().location().toString());
                int cavePlayerY = Minecraft.getInstance().player.getBlockY();
                for (var entry : bootstrap.clientController().caveSnapshots(caveDimension).entrySet()) {
                    var layer = bootstrap.clientController().projectCave(caveDimension, entry.getKey(), cavePlayerY,
                            com.muwenyan.simplemap.core.cave.CaveConfig.defaults());
                    if (layer.isPresent()) {
                        int cx = left + (entry.getKey().x() - region.origin().x()) * size;
                        int cz = top + (entry.getKey().z() - region.origin().z()) * size;
                        graphics.fill(cx, cz, cx + size, cz + size, layer.orElseThrow().argb());
                    }
                }
            }
            if (Minecraft.getInstance().player != null) {
                var player = Minecraft.getInstance().player;
                int px = left + (player.chunkPosition().x - region.origin().x()) * size;
                int pz = top + (player.chunkPosition().z - region.origin().z()) * size;
                graphics.fill(px - 2, pz - 2, px + 3, pz + 3, 0xFFFFFFFF);
                bootstrap.clientController().pin().filter(value -> value.dimension().equals(new DimensionId(player.level().dimension().location().toString()))).ifPresent(value -> {
                    int x = left + ((value.position().x() >> 4) - region.origin().x()) * size;
                    int z = top + ((value.position().z() >> 4) - region.origin().z()) * size;
                    graphics.fill(x - 2, z - 2, x + 3, z + 3, 0xFF00FFFF);
                    graphics.drawString(font, value.label(), x + 5, z - 5, 0xFF00FFFF, true);
                });
                double radians = Math.toRadians(player.getYRot());
                int dx = (int) Math.round(-Math.sin(radians) * size * 2);
                int dz = (int) Math.round(Math.cos(radians) * size * 2);
                graphics.fill(px, pz, px + dx, pz + dz, 0xFFFFFFFF);
                var dimension = new DimensionId(player.level().dimension().location().toString());
                for (var waypoint : bootstrap.clientController().visibleWaypoints(dimension)) {
                    int wx = left + ((waypoint.position().x() >> 4) - region.origin().x()) * size;
                    int wz = top + ((waypoint.position().z() >> 4) - region.origin().z()) * size;
                    int color = bootstrap.clientController().followedWaypoint().map(value -> value.id().equals(waypoint.id())).orElse(false) ? 0xFFFF4040 : 0xFFFFD040;
                    graphics.fill(wx - 2, wz - 2, wx + 3, wz + 3, color);
                    int distance = Math.max(Math.abs(waypoint.position().x() / 16 - player.chunkPosition().x), Math.abs(waypoint.position().z() / 16 - player.chunkPosition().z));
                    graphics.drawString(font, distance + "c", wx + 5, wz + 2, color, true);
                }
            }
        }
        graphics.drawCenteredString(font, Component.translatable("screen.simplemap.close"), width / 2, height - 30, 0xFFAAAAAA);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        bootstrap.clientController().zoomMinimap(delta > 0 ? 1.25d : 0.8d);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        bootstrap.clientController().runtime().navigation().pan(-dragX, -dragY);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) { bootstrap.clientController().clearFollowedWaypoint(); return true; }
        var region = bootstrap.clientController().runtime().currentRegion();
        var player = Minecraft.getInstance().player;
        if (region != null && player != null) {
            int size = Math.min(12, Math.max(1, Math.min(width, height) / 40));
            int left = width / 2 - 16 * size;
            int top = height / 2 - 16 * size;
            var dimension = new DimensionId(player.level().dimension().location().toString());
            for (var waypoint : bootstrap.clientController().visibleWaypoints(dimension)) {
                int wx = left + ((waypoint.position().x() >> 4) - region.origin().x()) * size;
                int wz = top + ((waypoint.position().z() >> 4) - region.origin().z()) * size;
                if (Math.abs(mouseX - wx) <= size && Math.abs(mouseY - wz) <= size) {
                    bootstrap.clientController().followWaypoint(waypoint.id(), dimension);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
