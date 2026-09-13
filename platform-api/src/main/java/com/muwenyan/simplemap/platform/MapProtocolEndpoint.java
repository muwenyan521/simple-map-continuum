package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.protocol.FrameCodec;
import com.muwenyan.simplemap.core.protocol.MapBookFrame;
import com.muwenyan.simplemap.core.protocol.ProtocolException;
import com.muwenyan.simplemap.core.protocol.MapBookMessageType;
import com.muwenyan.simplemap.core.protocol.WaypointSyncCodec;
import com.muwenyan.simplemap.core.protocol.WaypointSyncMessage;
import com.muwenyan.simplemap.core.protocol.MapBookAckAction;
import com.muwenyan.simplemap.core.protocol.MapBookAckCodec;
import com.muwenyan.simplemap.core.protocol.MapBookErrorCodec;
import com.muwenyan.simplemap.core.protocol.MapBookHello;
import com.muwenyan.simplemap.core.protocol.MapBookHelloCodec;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongSupplier;

public final class MapProtocolEndpoint {
    private final AtomicReference<MapBookFrame> lastFrame = new AtomicReference<>();
    private final AtomicReference<ProtocolException> lastError = new AtomicReference<>();
    private final AtomicLong received = new AtomicLong();
    private final AtomicReference<WaypointSyncMessage> lastWaypointSync = new AtomicReference<>();
    private final AtomicReference<MapBookTransferResult> lastTransferResult = new AtomicReference<>();
    private final AtomicReference<MapBookErrorCodec.RemoteError> lastRemoteError = new AtomicReference<>();
    private final AtomicReference<MapBookHello> lastRemoteHello = new AtomicReference<>();
    private volatile Consumer<MapBookFrame> observer;
    private volatile MapBookTransferService transferService;
    private volatile LongSupplier clock = System::currentTimeMillis;

    public MapProtocolEndpoint() { this(frame -> { }); }
    public MapProtocolEndpoint(Consumer<MapBookFrame> observer) { this.observer = Objects.requireNonNull(observer, "observer"); }
    public void setObserver(Consumer<MapBookFrame> observer) { this.observer = Objects.requireNonNull(observer, "observer"); }

    public void bindTransferService(MapBookTransferService service, LongSupplier clock) {
        this.transferService = Objects.requireNonNull(service, "service");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public void receive(byte[] payload) throws ProtocolException {
        MapBookFrame frame = FrameCodec.decode(payload);
        lastFrame.set(frame);
        if (frame.type() == MapBookMessageType.WAYPOINT_SYNC) lastWaypointSync.set(WaypointSyncCodec.decode(frame.body()));
        if (frame.type() == MapBookMessageType.HELLO || frame.type() == MapBookMessageType.HELLO_ACK) {
            lastRemoteHello.set(MapBookHelloCodec.decode(frame.body()));
        }
        if (frame.type() == MapBookMessageType.REGION_DATA && transferService != null) {
            try {
                lastTransferResult.set(transferService.accept(frame.sessionId(), frame.body(), clock.getAsLong()));
            } catch (ProtocolException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                throw new ProtocolException(com.muwenyan.simplemap.core.protocol.ProtocolErrorCode.MALFORMED_BODY,
                        "cannot route map book region", exception);
            }
        }
        if (frame.type() == MapBookMessageType.ACK && transferService != null) {
            try {
                MapBookAckAction action = MapBookAckCodec.decode(frame.body());
                lastTransferResult.set(action == MapBookAckAction.COMPLETE
                        ? transferService.complete(frame.sessionId(), clock.getAsLong())
                        : transferService.abort(frame.sessionId()));
            } catch (ProtocolException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                throw new ProtocolException(com.muwenyan.simplemap.core.protocol.ProtocolErrorCode.MALFORMED_BODY,
                        "cannot route map book acknowledgement", exception);
            }
        }
        if (frame.type() == MapBookMessageType.ERROR) {
            lastRemoteError.set(MapBookErrorCodec.decode(frame.body()));
        }
        observer.accept(frame);
        lastError.set(null);
        received.incrementAndGet();
    }

    public boolean receiveSafely(byte[] payload) {
        try {
            receive(payload);
            return true;
        } catch (ProtocolException exception) {
            lastError.set(exception);
            return false;
        }
    }

    public long receivedCount() { return received.get(); }
    public java.util.Optional<MapBookFrame> lastFrame() { return java.util.Optional.ofNullable(lastFrame.get()); }
    public java.util.Optional<ProtocolException> lastError() { return java.util.Optional.ofNullable(lastError.get()); }
    public java.util.Optional<WaypointSyncMessage> lastWaypointSync() { return java.util.Optional.ofNullable(lastWaypointSync.get()); }
    public java.util.Optional<MapBookTransferResult> lastTransferResult() { return java.util.Optional.ofNullable(lastTransferResult.get()); }
    public java.util.Optional<MapBookErrorCodec.RemoteError> lastRemoteError() { return java.util.Optional.ofNullable(lastRemoteError.get()); }
    public java.util.Optional<MapBookHello> lastRemoteHello() { return java.util.Optional.ofNullable(lastRemoteHello.get()); }

    public static byte[] encodeAckFrame(java.util.UUID sessionId, MapBookAckAction action) {
        try {
            return FrameCodec.encode(new MapBookFrame(FrameCodec.FRAME_VERSION, MapBookMessageType.ACK,
                    Objects.requireNonNull(sessionId, "sessionId"), MapBookAckCodec.encode(action)));
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("cannot encode map book acknowledgement", exception);
        }
    }

    public static byte[] encodeErrorFrame(java.util.UUID sessionId,
                                           com.muwenyan.simplemap.core.protocol.ProtocolErrorCode code,
                                           String message) {
        try {
            return FrameCodec.encode(new MapBookFrame(FrameCodec.FRAME_VERSION, MapBookMessageType.ERROR,
                    Objects.requireNonNull(sessionId, "sessionId"),
                    MapBookErrorCodec.encode(code, message)));
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("cannot encode map book error", exception);
        }
    }
}
