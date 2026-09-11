package com.muwenyan.simplemap.core.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.UUID;

public final class FrameCodec {
    public static final int FRAME_VERSION = 1;
    public static final int MAX_BODY_BYTES = 4 * 1024 * 1024;
    public static final int MAX_ARCHIVE_BYTES = MAX_BODY_BYTES;
    private static final int MAGIC = 0x534D4250;
    private FrameCodec() { }
    public static byte[] encode(MapBookFrame frame) throws IOException {
        if (frame.body().length > MAX_BODY_BYTES) throw new ProtocolException(ProtocolErrorCode.LIMIT_EXCEEDED, "frame body exceeds limit");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(32 + frame.body().length);
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeInt(MAGIC); out.writeByte(frame.frameVersion()); out.writeByte(frame.type().wireValue());
        out.writeLong(frame.sessionId().getMostSignificantBits()); out.writeLong(frame.sessionId().getLeastSignificantBits());
        out.writeInt(frame.body().length); out.write(frame.body()); out.flush();
        return bytes.toByteArray();
    }
    public static MapBookFrame decode(byte[] wire) throws ProtocolException {
        if (wire == null || wire.length < 26) throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "truncated frame");
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(wire));
            if (in.readInt() != MAGIC) throw new ProtocolException(ProtocolErrorCode.INVALID_MAGIC, "invalid frame magic");
            int version = in.readUnsignedByte();
            if (version != FRAME_VERSION) throw new ProtocolException(ProtocolErrorCode.UNSUPPORTED_VERSION, "unsupported frame version: " + version);
            MapBookMessageType type = MapBookMessageType.fromWire(in.readUnsignedByte());
            UUID session = new UUID(in.readLong(), in.readLong());
            int length = in.readInt();
            if (length < 0 || length > MAX_BODY_BYTES) throw new ProtocolException(ProtocolErrorCode.LIMIT_EXCEEDED, "invalid body length: " + length);
            if (length != in.available()) throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "body length does not match frame");
            return new MapBookFrame(version, type, session, in.readNBytes(length));
        } catch (EOFException e) { throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "truncated frame", e); }
        catch (IOException e) { if (e instanceof ProtocolException p) throw p; throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "cannot decode frame", e); }
    }
}
