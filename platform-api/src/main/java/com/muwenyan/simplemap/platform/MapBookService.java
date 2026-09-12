package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.book.MapBook;
import com.muwenyan.simplemap.core.book.MapBookRegion;
import com.muwenyan.simplemap.core.protocol.MapBookRegionCodec;
import com.muwenyan.simplemap.core.protocol.ProtocolException;
import java.util.Objects;
import java.util.UUID;
import java.util.List;

public final class MapBookService {
    private final MapBook book;
    private final int maxPayloadBytes;

    public MapBookService(MapBook book, int maxPayloadBytes) {
        this.book = Objects.requireNonNull(book, "book");
        if (maxPayloadBytes < 1) throw new IllegalArgumentException("maxPayloadBytes");
        this.maxPayloadBytes = maxPayloadBytes;
    }

    public MapBook book() { return book; }

    public void save(UUID actor, byte[] encodedRegion) throws ProtocolException {
        if (encodedRegion == null || encodedRegion.length > maxPayloadBytes) {
            throw new ProtocolException(com.muwenyan.simplemap.core.protocol.ProtocolErrorCode.LIMIT_EXCEEDED,
                    "region payload exceeds service limit");
        }
        MapBookRegion region = MapBookRegionCodec.decode(encodedRegion);
        book.save(actor, region.position(), region.payload());
    }

    public void merge(UUID actor, MapBook source) {
        book.merge(actor, source);
    }

    public int learn(UUID actor, List<byte[]> encodedRegions) throws ProtocolException {
        if (encodedRegions == null || encodedRegions.size() > 4096) {
            throw new ProtocolException(com.muwenyan.simplemap.core.protocol.ProtocolErrorCode.LIMIT_EXCEEDED,
                    "too many regions");
        }
        MapBook.LearningSession session = book.beginLearning(Objects.requireNonNull(actor, "actor"));
        try {
            for (byte[] encoded : encodedRegions) {
                if (encoded == null || encoded.length > maxPayloadBytes) {
                    throw new ProtocolException(com.muwenyan.simplemap.core.protocol.ProtocolErrorCode.LIMIT_EXCEEDED,
                            "region payload exceeds service limit");
                }
                MapBookRegion region = MapBookRegionCodec.decode(encoded);
                session.accept(region.position(), region.payload());
            }
            int staged = session.stagedCount();
            session.commit();
            return staged;
        } catch (ProtocolException | RuntimeException exception) {
            session.abort();
            if (exception instanceof ProtocolException protocolException) throw protocolException;
            throw exception;
        }
    }
}
