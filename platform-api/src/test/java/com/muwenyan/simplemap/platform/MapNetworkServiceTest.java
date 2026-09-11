package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.protocol.MapBookFrame;
import com.muwenyan.simplemap.core.protocol.MapBookMessageType;
import com.muwenyan.simplemap.core.protocol.MapPacketRouter;
import com.muwenyan.simplemap.platform.memory.LoopbackNetworkPort;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MapNetworkServiceTest {
    @Test
    void sendsAndReceivesEncodedFramesThroughPort() throws Exception {
        AtomicBoolean received = new AtomicBoolean();
        MapPacketRouter router = new MapPacketRouter();
        router.register(MapBookMessageType.ACK, frame -> received.set(true));
        MapNetworkService[] holder = new MapNetworkService[1];
        holder[0] = new MapNetworkService(new LoopbackNetworkPort(bytes -> {
            try { holder[0].receive(bytes); } catch (Exception exception) { throw new AssertionError(exception); }
        }), router);
        holder[0].send(new MapBookFrame(1, MapBookMessageType.ACK, UUID.randomUUID(), new byte[0])).join();
        assertTrue(received.get());
    }
}
