package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.protocol.FrameCodec;
import com.muwenyan.simplemap.core.protocol.MapBookFrame;
import com.muwenyan.simplemap.core.protocol.MapBookMessageType;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapProtocolEndpointTest {
    @Test
    void decodesAndRetainsLatestFrame() throws Exception {
        MapProtocolEndpoint endpoint = new MapProtocolEndpoint();
        MapBookFrame frame = new MapBookFrame(1, MapBookMessageType.HELLO, UUID.randomUUID(), new byte[]{1, 2});
        endpoint.receive(FrameCodec.encode(frame));
        assertEquals(frame, endpoint.lastFrame().orElseThrow());
        assertEquals(1, endpoint.receivedCount().get());
    }
}
