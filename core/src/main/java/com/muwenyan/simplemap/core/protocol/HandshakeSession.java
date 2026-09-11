package com.muwenyan.simplemap.core.protocol;

import java.util.Objects;

public final class HandshakeSession {
    private final MapBookHello local;
    private HandshakeState state = HandshakeState.INITIAL;
    private MapBookHello negotiated;

    public HandshakeSession(MapBookHello local) {
        this.local = Objects.requireNonNull(local, "local");
    }

    public synchronized MapBookHello local() {
        return local;
    }

    public synchronized HandshakeState state() {
        return state;
    }

    public synchronized MapBookHello sendHello() throws ProtocolException {
        if (state != HandshakeState.INITIAL) {
            throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "hello already sent");
        }
        state = HandshakeState.HELLO_SENT;
        return local;
    }

    public synchronized MapBookHello acceptHello(MapBookHello remote) throws ProtocolException {
        if (state != HandshakeState.HELLO_SENT) {
            state = HandshakeState.FAILED;
            throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "hello was not sent");
        }
        try {
            negotiated = MapBookNegotiator.negotiate(local, Objects.requireNonNull(remote, "remote"));
            state = HandshakeState.ESTABLISHED;
            return negotiated;
        } catch (ProtocolException exception) {
            state = HandshakeState.FAILED;
            throw exception;
        }
    }

    public synchronized MapBookHello negotiated() throws ProtocolException {
        if (state != HandshakeState.ESTABLISHED) {
            throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "handshake is not established");
        }
        return negotiated;
    }
}
