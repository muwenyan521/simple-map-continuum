package com.muwenyan.simplemap.core.streaming;

import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChunkEventRouterTest {
    @Test
    void forwardsLifecycleEventsToInjectedListener() {
        ChunkEventRouter router = new ChunkEventRouter(new ChunkStore(2));
        AtomicReference<ChunkEventKind> observed = new AtomicReference<>();
        router.listener(event -> observed.set(event.kind()));
        router.onEvent(new ChunkEvent(new DimensionId("minecraft:overworld"), new ChunkPos(0, 0),
                ChunkEventKind.BLOCK_CHANGED, 2));
        assertEquals(ChunkEventKind.BLOCK_CHANGED, observed.get());
    }
}
