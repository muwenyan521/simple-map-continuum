package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.book.MapBook;
import com.muwenyan.simplemap.core.protocol.ProtocolException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class MapBookTransferService {
    private final Map<UUID, MapBookTransferSession> sessions = new LinkedHashMap<>();
    private final int maxPayloadBytes;
    private final long timeoutMillis;

    public MapBookTransferService(int maxPayloadBytes, long timeoutMillis) {
        if (maxPayloadBytes < 1 || timeoutMillis < 1) {
            throw new IllegalArgumentException("invalid transfer limits");
        }
        this.maxPayloadBytes = maxPayloadBytes;
        this.timeoutMillis = timeoutMillis;
    }

    public synchronized UUID start(MapBook book, UUID actor, MapBookTransferMode mode, long nowMillis) {
        MapBookTransferSession session = new MapBookTransferSession(book, actor, mode,
                maxPayloadBytes, nowMillis, timeoutMillis);
        UUID id = UUID.randomUUID();
        sessions.put(id, session);
        return id;
    }

    public synchronized MapBookTransferResult accept(UUID sessionId, byte[] region, long nowMillis)
            throws ProtocolException {
        MapBookTransferSession session = session(sessionId);
        try {
            return session.acceptRegion(region, nowMillis);
        } catch (ProtocolException | RuntimeException exception) {
            if (session.state() != MapBookTransferState.ACTIVE) {
                sessions.remove(sessionId);
            }
            throw exception;
        }
    }

    public synchronized MapBookTransferResult complete(UUID sessionId, long nowMillis) {
        MapBookTransferSession session = session(sessionId);
        MapBookTransferResult result = session.complete(nowMillis);
        removeIfTerminal(sessionId, result);
        return result;
    }

    public synchronized MapBookTransferResult abort(UUID sessionId) {
        MapBookTransferResult result = session(sessionId).abort();
        removeIfTerminal(sessionId, result);
        return result;
    }

    public synchronized MapBookTransferResult tick(UUID sessionId, long nowMillis) {
        MapBookTransferResult result = session(sessionId).tick(nowMillis);
        removeIfTerminal(sessionId, result);
        return result;
    }

    public synchronized int activeCount() {
        return sessions.size();
    }

    private MapBookTransferSession session(UUID sessionId) {
        return Objects.requireNonNull(sessions.get(Objects.requireNonNull(sessionId, "sessionId")),
                "unknown transfer session");
    }

    private void removeIfTerminal(UUID sessionId, MapBookTransferResult result) {
        if (result.state() != MapBookTransferState.ACTIVE) {
            sessions.remove(sessionId);
        }
    }
}
