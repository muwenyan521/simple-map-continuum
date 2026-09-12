package com.muwenyan.simplemap.fabric;

import com.muwenyan.simplemap.core.info.BlockInfo;
import com.muwenyan.simplemap.core.info.WorldInfo;
import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.ChunkSnapshot;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.platform.port.WorldSourcePort;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

public final class Fabric1211WorldSource implements WorldSourcePort {
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
