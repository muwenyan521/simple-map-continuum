package com.muwenyan.simplemap.forge;

import com.muwenyan.simplemap.core.info.BlockInfo;
import com.muwenyan.simplemap.core.info.WorldInfo;
import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.ChunkSnapshot;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.platform.port.WorldSourcePort;
import com.muwenyan.simplemap.platform.port.SurfaceColumnSourcePort;
import com.muwenyan.simplemap.core.surface.SurfaceColumn;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import com.muwenyan.simplemap.core.cave.CaveColumnRun;
import com.muwenyan.simplemap.core.cave.CaveConfig;
import com.muwenyan.simplemap.platform.port.CaveColumnSourcePort;
import java.util.List;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("deprecation")
public final class Forge1201WorldSource implements WorldSourcePort, SurfaceColumnSourcePort, CaveColumnSourcePort {
    private final Supplier<ClientLevel> level;
    private final AtomicLong revision = new AtomicLong();

    public Forge1201WorldSource(Supplier<ClientLevel> level) {
        this.level = java.util.Objects.requireNonNull(level, "level");
    }

    @Override
    public Optional<ChunkSnapshot> snapshot(DimensionId dimension, ChunkPos position) {
        ClientLevel current = level.get();
        if (current == null || !dimension.equals(dimension(current))) return Optional.empty();
        ByteBuffer payload = ByteBuffer.allocate(16 * 16 * Integer.BYTES);
        for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
            int worldX = position.x() * 16 + x;
            int worldZ = position.z() * 16 + z;
            payload.putInt(current.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, worldX, worldZ));
        }
        return Optional.of(new ChunkSnapshot(dimension, position, revision.incrementAndGet(), payload.array()));
    }

    @Override
    public Optional<SurfaceColumn> sample(ChunkPos chunk, int localX, int localZ) {
        if (localX < 0 || localX >= 16 || localZ < 0 || localZ >= 16) throw new IllegalArgumentException("local coordinates out of range");
        ClientLevel current = level.get();
        if (current == null) return Optional.empty();
        int x = chunk.x() * 16 + localX;
        int z = chunk.z() * 16 + localZ;
        int y = current.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        net.minecraft.core.BlockPos nativePosition = new net.minecraft.core.BlockPos(x, y, z);
        var state = current.getBlockState(nativePosition);
        int color = state.getMapColor(current, nativePosition).col | 0xFF000000;
        int biomeColor = color;
        var biome = current.getBiome(nativePosition).value();
        if (state.is(net.minecraft.tags.BlockTags.LEAVES)) biomeColor = biome.getFoliageColor() | 0xFF000000;
        else if (!state.getFluidState().isEmpty()) biomeColor = biome.getWaterColor() | 0xFF000000;
        return Optional.of(new SurfaceColumn(localX, localZ, y, color, biomeColor,
                !state.getFluidState().isEmpty(), state.isSolidRender(current, nativePosition)));
    }

    @Override
    public List<CaveColumnRun> sample(ChunkPos chunk, int localX, int localZ, CaveConfig config) {
        ClientLevel current = level.get();
        if (current == null) return List.of();
        int x = chunk.x() * 16 + localX, z = chunk.z() * 16 + localZ;
        int top = current.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        List<CaveColumnRun> runs = new java.util.ArrayList<>();
        for (int y = Math.min(top, config.topY()); y >= current.getMinBuildHeight() && runs.size() < config.maxLayers(); y--) {
            net.minecraft.core.BlockPos position = new net.minecraft.core.BlockPos(x, y, z);
            var state = current.getBlockState(position);
            if (!state.isAir() && (state.isSolidRender(current, position) || !state.getFluidState().isEmpty())) {
                int color = state.getMapColor(current, position).col | 0xFF000000;
                runs.add(new CaveColumnRun(y, y, color, current.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, position), !state.getFluidState().isEmpty(), state.getLightEmission() > 0));
            }
        }
        return List.copyOf(runs);
    }

    @Override
    public WorldInfo inspect(DimensionId dimension, BlockPos position) {
        ClientLevel current = level.get();
        if (current == null || !dimension.equals(dimension(current))) return WorldInfo.empty(position);
        net.minecraft.core.BlockPos nativePosition = new net.minecraft.core.BlockPos(position.x(), position.y(), position.z());
        var state = current.getBlockState(nativePosition);
        var block = state.getBlock();
        BlockInfo info = new BlockInfo(BuiltInRegistries.BLOCK.getKey(block).toString(),
                block.getName().getString(), 0xFFFFFFFF, !state.getFluidState().isEmpty(),
                state.isSolidRender(current, nativePosition));
        return new WorldInfo(Optional.of(info), Optional.empty(), position);
    }

    private static DimensionId dimension(ClientLevel level) {
        return new DimensionId(level.dimension().location().toString());
    }
}
