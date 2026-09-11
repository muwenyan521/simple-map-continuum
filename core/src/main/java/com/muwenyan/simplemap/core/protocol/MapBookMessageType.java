package com.muwenyan.simplemap.core.protocol;

public enum MapBookMessageType {
    HELLO(1), HELLO_ACK(2), REGION_DATA(3), ACK(4), ERROR(5);
    private final int wireValue;
    MapBookMessageType(int wireValue) { this.wireValue = wireValue; }
    public int wireValue() { return wireValue; }
    public static MapBookMessageType fromWire(int value) throws ProtocolException {
        for (MapBookMessageType type : values()) if (type.wireValue == value) return type;
        throw new ProtocolException(ProtocolErrorCode.INVALID_TYPE, "unknown message type: " + value);
    }
}
