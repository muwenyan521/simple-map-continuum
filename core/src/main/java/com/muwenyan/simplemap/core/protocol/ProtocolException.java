package com.muwenyan.simplemap.core.protocol;

import java.io.IOException;

public final class ProtocolException extends IOException {
    private static final long serialVersionUID = 1L;
    private final ProtocolErrorCode code;
    public ProtocolException(ProtocolErrorCode code, String message) { super(message); this.code = code; }
    public ProtocolException(ProtocolErrorCode code, String message, Throwable cause) { super(message, cause); this.code = code; }
    public ProtocolErrorCode code() { return code; }
}
