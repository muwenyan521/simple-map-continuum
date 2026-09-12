package com.muwenyan.simplemap.core.persistence;

import java.util.Objects;

public record Smr2Record(Smr2RecordType type, int localKey, long sourceRevision,
                         long styleRevision, byte[] payload) {
    public Smr2Record {
        type = Objects.requireNonNull(type, "type");
        if (sourceRevision < 0 || styleRevision < 0) throw new IllegalArgumentException("negative revision");
        payload = Objects.requireNonNull(payload, "payload").clone();
        if (payload.length > ArchiveCodec.MAX_PAYLOAD_BYTES) throw new IllegalArgumentException("payload exceeds limit");
    }

    @Override
    public byte[] payload() { return payload.clone(); }

    public RecordKey key() { return new RecordKey(type, localKey); }
    public record RecordKey(Smr2RecordType type, int localKey) { }
}
