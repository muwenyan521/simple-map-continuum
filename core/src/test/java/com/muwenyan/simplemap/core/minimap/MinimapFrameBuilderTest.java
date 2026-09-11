package com.muwenyan.simplemap.core.minimap;

import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.navigation.PlayerMapState;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MinimapFrameBuilderTest {
    @Test
    void buildsFrameFromPlayerState() {
        PlayerMapState player = new PlayerMapState(new DimensionId("minecraft:overworld"), 1, 2, 64, 45);
        MinimapFrame frame = MinimapFrameBuilder.build(player, MinimapConfig.defaults(), 3,
                List.of(new MinimapTile(new ChunkPos(0, 0), 0xff000000, 1)));
        assertEquals(3, frame.generation());
        assertEquals(player.dimension(), frame.dimension());
    }
}
