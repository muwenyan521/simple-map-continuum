package com.muwenyan.simplemap.core.book;

import com.muwenyan.simplemap.core.model.RegionPos;
import java.util.Objects;

public record MapBookRegion(RegionPos position, long revision, byte[] payload) {
    public MapBookRegion {
        position = Objects.requireNonNull(position, "position");
        if (revision < 0) {
            throw new IllegalArgumentException("revision must be non-negative");
        }
        payload = Objects.requireNonNull(payload, "payload").clone();
    }

    @Override
    public byte[] payload() {
        return payload.clone();
    }
}
