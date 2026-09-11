package com.muwenyan.simplemap.core.persistence;

public enum ArchiveFormat {
    SMAP("SMAP", 1), SMR2("SMR2", 2), CVR("CVR", 1);
    private final String magic;
    private final int version;
    ArchiveFormat(String magic, int version) { this.magic = magic; this.version = version; }
    public String magic() { return magic; }
    public int version() { return version; }
}
