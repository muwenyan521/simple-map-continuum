package com.muwenyan.simplemap.core.protocol;

import com.muwenyan.simplemap.core.book.MapBookRegion;
import com.muwenyan.simplemap.core.model.RegionPos;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.zip.CRC32;

public final class MapBookRegionCodec {
    private static final int MAGIC = 0x52474D31;
    private static final int VERSION = 1;
    private static final int MAX_PAYLOAD = FrameCodec.MAX_BODY_BYTES;

    private MapBookRegionCodec() {
    }

    public static byte[] encode(MapBookRegion region) throws ProtocolException {
        if (region == null || region.payload().length > MAX_PAYLOAD) {
            throw new ProtocolException(ProtocolErrorCode.LIMIT_EXCEEDED, "invalid region payload");
        }
        byte[] payload = region.payload();
        CRC32 crc = new CRC32();
        crc.update(payload);
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(32 + payload.length);
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeInt(MAGIC);
            out.writeByte(VERSION);
            out.writeInt(region.position().x());
            out.writeInt(region.position().z());
            out.writeLong(region.revision());
            out.writeInt(payload.length);
            out.write(payload);
            out.writeInt((int) crc.getValue());
            out.flush();
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "cannot encode region", exception);
        }
    }

    public static MapBookRegion decode(byte[] wire) throws ProtocolException {
        if (wire == null || wire.length < 29 || wire.length > MAX_PAYLOAD + 29) {
            throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "invalid region wire length");
        }
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(wire));
            if (in.readInt() != MAGIC || in.readUnsignedByte() != VERSION) {
                throw new ProtocolException(ProtocolErrorCode.INVALID_MAGIC, "invalid region header");
            }
            RegionPos position = new RegionPos(in.readInt(), in.readInt());
            long revision = in.readLong();
            int length = in.readInt();
            if (length < 1 || length > MAX_PAYLOAD || in.available() != length + 4) {
                throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "invalid region payload length");
            }
            byte[] payload = in.readNBytes(length);
            int expected = in.readInt();
            CRC32 crc = new CRC32();
            crc.update(payload);
            if ((int) crc.getValue() != expected) {
                throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "region payload CRC mismatch");
            }
            return new MapBookRegion(position, revision, payload);
        } catch (EOFException exception) {
            throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "truncated region", exception);
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ProtocolException protocolException) {
                throw protocolException;
            }
            throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "cannot decode region", exception);
        }
    }
}
