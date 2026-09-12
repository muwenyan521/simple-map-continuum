package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.protocol.FrameCodec;
import com.muwenyan.simplemap.core.protocol.MapBookFrame;
import com.muwenyan.simplemap.core.protocol.MapBookMessageType;
import com.muwenyan.simplemap.core.protocol.WaypointSyncCodec;
import com.muwenyan.simplemap.core.protocol.WaypointSyncMessage;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MapProtocolEndpointTest {
    @Test
    void decodesAndRetainsLatestFrame() throws Exception {
        MapProtocolEndpoint endpoint = new MapProtocolEndpoint();
        MapBookFrame frame = new MapBookFrame(1, MapBookMessageType.HELLO, UUID.randomUUID(), new byte[]{1, 2});
        endpoint.receive(FrameCodec.encode(frame));
        assertEquals(frame, endpoint.lastFrame().orElseThrow());
        assertEquals(1, endpoint.receivedCount());
    }

    @Test
    void retainsProtocolFailureForDiagnostics() {
        MapProtocolEndpoint endpoint = new MapProtocolEndpoint();
        assertFalse(endpoint.receiveSafely(new byte[]{1, 2, 3}));
        assertEquals(true, endpoint.lastError().isPresent());
        assertEquals(0, endpoint.receivedCount());
    }

    @Test
    void extractsWaypointSyncFrames() throws Exception {
        MapProtocolEndpoint endpoint = new MapProtocolEndpoint();
        var message = new WaypointSyncMessage(4, java.util.List.of());
        var frame = new MapBookFrame(1, MapBookMessageType.WAYPOINT_SYNC, UUID.randomUUID(), WaypointSyncCodec.encode(message));
        endpoint.receive(FrameCodec.encode(frame));
        assertEquals(4, endpoint.lastWaypointSync().orElseThrow().revision());
    }

    @Test
    void notifiesObserverAfterFrameValidation() throws Exception {
        var observed = new java.util.concurrent.atomic.AtomicReference<MapBookFrame>();
        MapProtocolEndpoint endpoint = new MapProtocolEndpoint(observed::set);
        MapBookFrame frame = new MapBookFrame(1, MapBookMessageType.HELLO, UUID.randomUUID(), new byte[]{3});
        endpoint.receive(FrameCodec.encode(frame));
        assertEquals(frame, observed.get());
    }
}
