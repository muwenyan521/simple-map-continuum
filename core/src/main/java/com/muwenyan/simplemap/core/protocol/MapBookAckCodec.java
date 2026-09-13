package com.muwenyan.simplemap.core.protocol;

public final class MapBookAckCodec {
    private MapBookAckCodec() { }
    public static byte[] encode(MapBookAckAction action) throws ProtocolException {
        if (action == null) throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "ack action is null");
        return new byte[] {(byte) action.wireValue()};
    }
    public static MapBookAckAction decode(byte[] body) throws ProtocolException {
        if (body == null || body.length != 1) throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "invalid ack body");
        return MapBookAckAction.fromWire(Byte.toUnsignedInt(body[0]));
    }
}
