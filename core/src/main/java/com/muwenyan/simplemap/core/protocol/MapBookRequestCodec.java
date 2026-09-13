package com.muwenyan.simplemap.core.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.UUID;

public final class MapBookRequestCodec {
    private MapBookRequestCodec() { }
    public static byte[] encode(MapBookRequest request) throws ProtocolException {
        if (request == null) throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "request is null");
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(17);
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeLong(request.bookId().getMostSignificantBits());
            out.writeLong(request.bookId().getLeastSignificantBits());
            out.writeByte(request.operation().wireValue());
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "cannot encode request", exception);
        }
    }
    public static MapBookRequest decode(byte[] body) throws ProtocolException {
        if (body == null || body.length != 17) throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "invalid request body");
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(body));
            MapBookRequest request = new MapBookRequest(new UUID(in.readLong(), in.readLong()),
                    MapBookOperation.fromWire(in.readUnsignedByte()));
            if (in.available() != 0) throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "trailing request bytes");
            return request;
        } catch (EOFException exception) {
            throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "truncated request", exception);
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ProtocolException protocolException) throw protocolException;
            throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "cannot decode request", exception);
        }
    }
}
