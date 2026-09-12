package com.muwenyan.simplemap.fabric;

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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import com.muwenyan.simplemap.core.cave.CaveColumnRun;
import com.muwenyan.simplemap.core.cave.CaveConfig;
import com.muwenyan.simplemap.platform.port.CaveColumnSourcePort;
import java.util.List;

public final class Fabric1211WorldSource implements WorldSourcePort, SurfaceColumnSourcePort, CaveColumnSourcePort {
    private final Supplier<ClientLevel> level;

    public Fabric1211WorldSource(Supplier<ClientLevel> level) {
        this.level = java.util.Objects.requireNonNull(level, "level");
    }

    @Override
    public Optional<ChunkSnapshot> snapshot(DimensionId dimension, ChunkPos position) {
        ClientLevel current = level.get();
        if (current == null || !dimension.equals(dimension(current))) return Optional.empty();
        return Optional.of(new ChunkSnapshot(dimension, position, 0, new byte[0]));
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
        BlockState state = current.getBlockState(nativePosition);
        int color = state.getMapColor(current, nativePosition).col | 0xFF000000;
        return Optional.of(new SurfaceColumn(localX, localZ, y, color, !state.getFluidState().isEmpty(), state.isSolidRender(current, nativePosition)));
    }

    @Override
    public List<CaveColumnRun> sample(ChunkPos chunk, int localX, int localZ, CaveConfig config) {
        ClientLevel current = level.get();
        if (current == null) return List.of();
        int x = chunk.x() * 16 + localX;
        int z = chunk.z() * 16 + localZ;
        int top = current.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        for (int y = Math.min(top, config.topY()); y >= current.getMinBuildHeight(); y--) {
            net.minecraft.core.BlockPos position = new net.minecraft.core.BlockPos(x, y, z);
            BlockState state = current.getBlockState(position);
            if (!state.isAir() && state.isSolidRender(current, position)) {
                int color = state.getMapColor(current, position).col | 0xFF000000;
                return List.of(new CaveColumnRun(y, y, color, current.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, position), !state.getFluidState().isEmpty(), state.getLightEmission() > 0));
            }
        }
        return List.of();
    }

    @Override
    public WorldInfo inspect(DimensionId dimension, BlockPos position) {
        ClientLevel current = level.get();
        if (current == null || !dimension.equals(dimension(current))) return WorldInfo.empty(position);
        net.minecraft.core.BlockPos nativePosition = new net.minecraft.core.BlockPos(position.x(), position.y(), position.z());
        BlockState state = current.getBlockState(nativePosition);
        var block = state.getBlock();
        String id = BuiltInRegistries.BLOCK.getKey(block).toString();
        BlockInfo info = new BlockInfo(id, block.getName().getString(), 0xFFFFFFFF,
                !state.getFluidState().isEmpty(), state.isSolidRender(current, nativePosition));
        return new WorldInfo(Optional.of(info), Optional.empty(), position);
    }

    private static DimensionId dimension(ClientLevel level) {
        return new DimensionId(level.dimension().location().toString());
    }
}
