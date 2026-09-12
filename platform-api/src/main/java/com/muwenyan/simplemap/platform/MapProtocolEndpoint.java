package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.protocol.FrameCodec;
import com.muwenyan.simplemap.core.protocol.MapBookFrame;
import com.muwenyan.simplemap.core.protocol.ProtocolException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class MapProtocolEndpoint {
    private final AtomicReference<MapBookFrame> lastFrame = new AtomicReference<>();
    private final AtomicLong received = new AtomicLong();

    public void receive(byte[] payload) throws ProtocolException {
        MapBookFrame frame = FrameCodec.decode(payload);
        lastFrame.set(frame);
        received.incrementAndGet();
    }

    public long receivedCount() { return received.get(); }
    public java.util.Optional<MapBookFrame> lastFrame() { return java.util.Optional.ofNullable(lastFrame.get()); }
}
