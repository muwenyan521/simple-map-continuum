package com.muwenyan.simplemap.core.protocol;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public record MapBookFrame(int frameVersion, MapBookMessageType type, UUID sessionId, byte[] body) {
    public MapBookFrame {
        if (frameVersion != FrameCodec.FRAME_VERSION) throw new IllegalArgumentException("unsupported frame version");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(body, "body");
        if (body.length > FrameCodec.MAX_BODY_BYTES) throw new IllegalArgumentException("body too large");
        body = body.clone();
    }
    @Override public byte[] body() { return body.clone(); }
    @Override public boolean equals(Object other) { return other instanceof MapBookFrame that && frameVersion == that.frameVersion && type == that.type && sessionId.equals(that.sessionId) && Arrays.equals(body, that.body); }
    @Override public int hashCode() { return 31 * (31 * (31 + frameVersion) + type.hashCode()) + Arrays.hashCode(body); }
}
