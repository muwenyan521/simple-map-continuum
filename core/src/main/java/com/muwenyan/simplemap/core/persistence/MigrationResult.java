package com.muwenyan.simplemap.core.persistence;

public record MigrationResult(boolean migrated, byte[] payload, String sourceFormat) {
    public MigrationResult { if (payload == null) throw new IllegalArgumentException("payload"); }
    @Override public byte[] payload() { return payload.clone(); }
}
