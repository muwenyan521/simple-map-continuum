package com.muwenyan.simplemap.core.protocol;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public final class MapPacketRouter {
    private final Map<MapBookMessageType, Consumer<MapBookFrame>> handlers = new EnumMap<>(MapBookMessageType.class);

    public synchronized void register(MapBookMessageType type, Consumer<MapBookFrame> handler) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(handler, "handler");
        if (handlers.putIfAbsent(type, handler) != null) {
            throw new IllegalStateException("handler already registered: " + type);
        }
    }

    public void dispatch(byte[] encoded) throws ProtocolException {
        MapBookFrame frame = FrameCodec.decode(encoded);
        Consumer<MapBookFrame> handler;
        synchronized (this) {
            handler = handlers.get(frame.type());
        }
        if (handler == null) {
            throw new ProtocolException(ProtocolErrorCode.INVALID_TYPE, "no handler for: " + frame.type());
        }
        handler.accept(frame);
    }
}
