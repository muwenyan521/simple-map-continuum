package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.book.MapBook;
import com.muwenyan.simplemap.core.book.MapBookRegion;
import com.muwenyan.simplemap.core.protocol.MapBookRegionCodec;
import com.muwenyan.simplemap.core.protocol.ProtocolException;
import java.util.Objects;
import java.util.UUID;

public final class MapBookTransferSession {
    private final MapBook book;
    private final UUID actor;
    private final MapBookTransferMode mode;
    private final int maxPayloadBytes;
    private final long timeoutMillis;
    private final long createdAtMillis;
    private MapBook.LearningSession learning;
    private MapBookTransferState state = MapBookTransferState.ACTIVE;
    private int acceptedRegions;
    private long lastActivityMillis;

    public MapBookTransferSession(MapBook book, UUID actor, MapBookTransferMode mode,
                                  int maxPayloadBytes, long nowMillis, long timeoutMillis) {
        this.book = Objects.requireNonNull(book, "book");
        this.actor = Objects.requireNonNull(actor, "actor");
        this.mode = Objects.requireNonNull(mode, "mode");
        if (maxPayloadBytes < 1 || timeoutMillis < 1) {
            throw new IllegalArgumentException("invalid transfer limits");
        }
        this.maxPayloadBytes = maxPayloadBytes;
        this.timeoutMillis = timeoutMillis;
        this.createdAtMillis = nowMillis;
        this.lastActivityMillis = nowMillis;
        if (mode == MapBookTransferMode.LEARN) {
            learning = book.beginLearning(actor);
        }
    }

    public synchronized MapBookTransferState state() {
        return state;
    }

    public synchronized int acceptedRegions() {
        return acceptedRegions;
    }

    public synchronized long createdAtMillis() {
        return createdAtMillis;
    }

    public synchronized MapBookTransferResult acceptRegion(byte[] encodedRegion, long nowMillis)
            throws ProtocolException {
        requireActive();
        checkTimeout(nowMillis);
        if (state != MapBookTransferState.ACTIVE) {
            return result();
        }
        if (encodedRegion == null || encodedRegion.length > maxPayloadBytes) {
            abort();
            throw new ProtocolException(com.muwenyan.simplemap.core.protocol.ProtocolErrorCode.LIMIT_EXCEEDED,
                    "region payload exceeds transfer limit");
        }
        final MapBookRegion region;
        try {
            region = MapBookRegionCodec.decode(encodedRegion);
            if (mode == MapBookTransferMode.SAVE) {
                book.save(actor, region.position(), region.payload());
            } else {
                learning.accept(region.position(), region.payload());
            }
        } catch (ProtocolException | RuntimeException exception) {
            abort();
            throw exception;
        }
        acceptedRegions++;
        lastActivityMillis = nowMillis;
        return result();
    }

    public synchronized MapBookTransferResult complete(long nowMillis) {
        requireActive();
        checkTimeout(nowMillis);
        if (state != MapBookTransferState.ACTIVE) {
            return result();
        }
        if (learning != null) {
            learning.commit();
            learning = null;
        }
        state = MapBookTransferState.COMPLETED;
        lastActivityMillis = nowMillis;
        return result();
    }

    public synchronized MapBookTransferResult abort() {
        if (state == MapBookTransferState.ACTIVE) {
            abortLearning();
            state = MapBookTransferState.ABORTED;
        }
        return result();
    }

    public synchronized MapBookTransferResult tick(long nowMillis) {
        if (state == MapBookTransferState.ACTIVE) {
            checkTimeout(nowMillis);
        }
        return result();
    }

    private void checkTimeout(long nowMillis) {
        if (nowMillis < lastActivityMillis) {
            throw new IllegalArgumentException("time moved backwards");
        }
        if (nowMillis - lastActivityMillis >= timeoutMillis) {
            abortLearning();
            state = MapBookTransferState.TIMED_OUT;
        }
    }

    private void requireActive() {
        if (state != MapBookTransferState.ACTIVE) {
            throw new IllegalStateException("transfer is not active: " + state);
        }
    }

    private void abortLearning() {
        if (learning != null) {
            learning.abort();
            learning = null;
        }
    }

    private MapBookTransferResult result() {
        return new MapBookTransferResult(state, acceptedRegions);
    }
}
