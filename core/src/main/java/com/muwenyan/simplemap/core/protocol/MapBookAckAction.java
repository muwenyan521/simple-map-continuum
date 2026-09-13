package com.muwenyan.simplemap.core.protocol;

public enum MapBookAckAction {
    COMPLETE(1),
    ABORT(2);

    private final int wireValue;
    MapBookAckAction(int wireValue) { this.wireValue = wireValue; }
    public int wireValue() { return wireValue; }

    public static MapBookAckAction fromWire(int value) throws ProtocolException {
        for (MapBookAckAction action : values()) if (action.wireValue == value) return action;
        throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "unknown ack action: " + value);
    }
}
