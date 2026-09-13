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

    @Test
    void routesRegionDataIntoBoundTransferService() throws Exception {
        UUID owner = UUID.randomUUID();
        var book = new com.muwenyan.simplemap.core.book.MapBook(UUID.randomUUID(), owner);
        var service = new MapBookTransferService(1024, 1000);
        UUID session = service.start(book, owner, MapBookTransferMode.SAVE, 0);
        var endpoint = new MapProtocolEndpoint();
        endpoint.bindTransferService(service, () -> 1L);
        byte[] region = com.muwenyan.simplemap.core.protocol.MapBookRegionCodec.encode(
                new com.muwenyan.simplemap.core.book.MapBookRegion(
                        new com.muwenyan.simplemap.core.model.RegionPos(0, 0), 1, new byte[]{7}));
        endpoint.receive(FrameCodec.encode(new MapBookFrame(1, MapBookMessageType.REGION_DATA, session, region)));
        assertEquals(MapBookTransferState.ACTIVE, endpoint.lastTransferResult().orElseThrow().state());
        assertEquals(1, book.snapshot().regions().size());
    }

    @Test
    void rejectsRegionDataForUnknownTransferSession() throws Exception {
        var endpoint = new MapProtocolEndpoint();
        endpoint.bindTransferService(new MapBookTransferService(1024, 1000), () -> 1L);
        assertFalse(endpoint.receiveSafely(FrameCodec.encode(new MapBookFrame(1, MapBookMessageType.REGION_DATA,
                UUID.randomUUID(), new byte[]{1}))));
        assertEquals(com.muwenyan.simplemap.core.protocol.ProtocolErrorCode.MALFORMED_BODY,
                endpoint.lastError().orElseThrow().code());
    }
}
