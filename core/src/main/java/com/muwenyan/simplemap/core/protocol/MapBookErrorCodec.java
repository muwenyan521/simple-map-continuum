package com.muwenyan.simplemap.core.protocol;

import java.nio.charset.StandardCharsets;

public final class MapBookErrorCodec {
    private static final int MAX_MESSAGE_BYTES = 512;
    private MapBookErrorCodec() { }
    public static byte[] encode(ProtocolErrorCode code, String message) throws ProtocolException {
        if (code == null || message == null) throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "error fields are null");
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_MESSAGE_BYTES) throw new ProtocolException(ProtocolErrorCode.LIMIT_EXCEEDED, "error message too long");
        byte[] body = new byte[3 + bytes.length];
        body[0] = (byte) code.ordinal();
        body[1] = (byte) (bytes.length >>> 8);
        body[2] = (byte) bytes.length;
        System.arraycopy(bytes, 0, body, 3, bytes.length);
        return body;
    }
    public static RemoteError decode(byte[] body) throws ProtocolException {
        if (body == null || body.length < 3) throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "invalid error body");
        int code = Byte.toUnsignedInt(body[0]);
        if (code >= ProtocolErrorCode.values().length) throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "unknown error code");
        int length = Byte.toUnsignedInt(body[1]) << 8 | Byte.toUnsignedInt(body[2]);
        if (length > MAX_MESSAGE_BYTES || body.length != length + 3) throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "invalid error message length");
        return new RemoteError(ProtocolErrorCode.values()[code], new String(body, 3, length, StandardCharsets.UTF_8));
    }
    public record RemoteError(ProtocolErrorCode code, String message) { }
}
