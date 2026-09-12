package com.muwenyan.simplemap.core.persistence;

public enum Smr2RecordType {
    SURFACE_SOURCE(1),
    CAVE_ARCHIVE(2),
    SURFACE_DERIVED(16),
    CAVE_DERIVED(17),
    MIGRATION_MARKER(31);

    private final int code;

    Smr2RecordType(int code) { this.code = code; }
    public int code() { return code; }

    public static Smr2RecordType fromCode(int code) throws ArchiveException {
        for (Smr2RecordType type : values()) {
            if (type.code == code) return type;
        }
        throw new ArchiveException("unknown SMR2 record type: " + code);
    }
}
