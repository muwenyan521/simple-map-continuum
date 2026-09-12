package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.protocol.FrameCodec;
import com.muwenyan.simplemap.core.protocol.MapBookFrame;
import com.muwenyan.simplemap.core.protocol.ProtocolException;
import com.muwenyan.simplemap.core.protocol.MapBookMessageType;
import com.muwenyan.simplemap.core.protocol.WaypointSyncCodec;
import com.muwenyan.simplemap.core.protocol.WaypointSyncMessage;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class MapProtocolEndpoint {
    private final AtomicReference<MapBookFrame> lastFrame = new AtomicReference<>();
    private final AtomicReference<ProtocolException> lastError = new AtomicReference<>();
    private final AtomicLong received = new AtomicLong();
    private final AtomicReference<WaypointSyncMessage> lastWaypointSync = new AtomicReference<>();
    private final Consumer<MapBookFrame> observer;

    public MapProtocolEndpoint() { this(frame -> { }); }
    public MapProtocolEndpoint(Consumer<MapBookFrame> observer) { this.observer = Objects.requireNonNull(observer, "observer"); }

    public void receive(byte[] payload) throws ProtocolException {
        MapBookFrame frame = FrameCodec.decode(payload);
        lastFrame.set(frame);
        if (frame.type() == MapBookMessageType.WAYPOINT_SYNC) lastWaypointSync.set(WaypointSyncCodec.decode(frame.body()));
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
}
