package com.muwenyan.simplemap.core.protocol;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapPacketRouterTest {
    @Test
    void dispatchesTypedFrameExactlyOnce() throws Exception {
        AtomicBoolean received = new AtomicBoolean();
        MapPacketRouter router = new MapPacketRouter();
        router.register(MapBookMessageType.ACK, frame -> received.set(true));
        byte[] wire = FrameCodec.encode(new MapBookFrame(1, MapBookMessageType.ACK, UUID.randomUUID(), new byte[]{1}));
        router.dispatch(wire);
        assertTrue(received.get());
        assertThrows(IllegalStateException.class, () -> router.register(MapBookMessageType.ACK, frame -> { }));
    }
}
