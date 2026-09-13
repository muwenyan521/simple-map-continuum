package com.muwenyan.simplemap.core.protocol;

public enum MapBookOperation {
    SAVE(1),
    LEARN(2);

    private final int wireValue;
    MapBookOperation(int wireValue) { this.wireValue = wireValue; }
    public int wireValue() { return wireValue; }
    public static MapBookOperation fromWire(int value) throws ProtocolException {
        for (MapBookOperation operation : values()) if (operation.wireValue == value) return operation;
        throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "unknown map book operation: " + value);
    }
}
