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
        var hello = new com.muwenyan.simplemap.core.protocol.MapBookHello(1, 0, 4096,
                java.util.Set.of("SMAP"), java.util.Set.of("REGION_DATA"));
        MapBookFrame frame = new MapBookFrame(1, MapBookMessageType.HELLO, UUID.randomUUID(),
                com.muwenyan.simplemap.core.protocol.MapBookHelloCodec.encode(hello));
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
        var hello = new com.muwenyan.simplemap.core.protocol.MapBookHello(1, 0, 4096,
                java.util.Set.of("SMAP"), java.util.Set.of("REGION_DATA"));
        MapBookFrame frame = new MapBookFrame(1, MapBookMessageType.HELLO, UUID.randomUUID(),
                com.muwenyan.simplemap.core.protocol.MapBookHelloCodec.encode(hello));
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

    @Test
    void ackCompletesBoundTransferAndErrorIsRetained() throws Exception {
        UUID owner = UUID.randomUUID();
        var book = new com.muwenyan.simplemap.core.book.MapBook(UUID.randomUUID(), owner);
        var service = new MapBookTransferService(1024, 1000);
        UUID session = service.start(book, owner, MapBookTransferMode.SAVE, 0);
        var endpoint = new MapProtocolEndpoint();
        endpoint.bindTransferService(service, () -> 1L);
        endpoint.receive(FrameCodec.encode(new MapBookFrame(1, MapBookMessageType.ACK, session,
                com.muwenyan.simplemap.core.protocol.MapBookAckCodec.encode(
                        com.muwenyan.simplemap.core.protocol.MapBookAckAction.COMPLETE))));
        assertEquals(MapBookTransferState.COMPLETED, endpoint.lastTransferResult().orElseThrow().state());
        var error = new MapBookFrame(1, MapBookMessageType.ERROR, UUID.randomUUID(),
                com.muwenyan.simplemap.core.protocol.MapBookErrorCodec.encode(
                        com.muwenyan.simplemap.core.protocol.ProtocolErrorCode.LIMIT_EXCEEDED, "too large"));
        endpoint.receive(FrameCodec.encode(error));
        assertEquals(com.muwenyan.simplemap.core.protocol.ProtocolErrorCode.LIMIT_EXCEEDED,
                endpoint.lastRemoteError().orElseThrow().code());
    }

    @Test
    void errorCodecPreservesMessagesLongerThanOneByte() throws Exception {
        String message = "x".repeat(300);
        var decoded = com.muwenyan.simplemap.core.protocol.MapBookErrorCodec.decode(
                com.muwenyan.simplemap.core.protocol.MapBookErrorCodec.encode(
                        com.muwenyan.simplemap.core.protocol.ProtocolErrorCode.MALFORMED_BODY, message));
        assertEquals(message, decoded.message());
    }

    @Test
    void retainsDecodedHelloFrames() throws Exception {
        var hello = new com.muwenyan.simplemap.core.protocol.MapBookHello(1, 0, 4096,
                java.util.Set.of("SMAP"), java.util.Set.of("REGION_DATA"));
        var endpoint = new MapProtocolEndpoint();
        endpoint.receive(FrameCodec.encode(new MapBookFrame(1, MapBookMessageType.HELLO, UUID.randomUUID(),
                com.muwenyan.simplemap.core.protocol.MapBookHelloCodec.encode(hello))));
        assertEquals(hello, endpoint.lastRemoteHello().orElseThrow());
    }

    @Test
    void unknownAckSessionIsReportedAsProtocolFailure() throws Exception {
        var endpoint = new MapProtocolEndpoint();
        endpoint.bindTransferService(new MapBookTransferService(1024, 1000), () -> 1L);
        assertFalse(endpoint.receiveSafely(FrameCodec.encode(new MapBookFrame(1, MapBookMessageType.ACK,
                UUID.randomUUID(), com.muwenyan.simplemap.core.protocol.MapBookAckCodec.encode(
                        com.muwenyan.simplemap.core.protocol.MapBookAckAction.COMPLETE)))));
        assertEquals(com.muwenyan.simplemap.core.protocol.ProtocolErrorCode.MALFORMED_BODY,
                endpoint.lastError().orElseThrow().code());
    }

    @Test
    void encodesAckAndErrorFramesForTransportAdapters() throws Exception {
        UUID session = UUID.randomUUID();
        assertEquals(MapBookMessageType.ACK,
                FrameCodec.decode(MapProtocolEndpoint.encodeAckFrame(session,
                        com.muwenyan.simplemap.core.protocol.MapBookAckAction.COMPLETE)).type());
        assertEquals(MapBookMessageType.ERROR,
                FrameCodec.decode(MapProtocolEndpoint.encodeErrorFrame(session,
                        com.muwenyan.simplemap.core.protocol.ProtocolErrorCode.MALFORMED_BODY, "bad")).type());
    }
}
