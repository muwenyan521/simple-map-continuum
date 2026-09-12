package com.muwenyan.simplemap.core.persistence;

public record Smr2Header(long worldIdentity, int regionX, int regionZ, int dataVersion) {
    public Smr2Header {
        if (dataVersion < 1) throw new IllegalArgumentException("data version must be positive");
    }
}
