package com.muwenyan.simplemap.core.persistence;

public enum ArchiveFormat {
    SMAP("SMAP", 6, 1, 6), SMR2("SMR2", 2, 2, 2), CVR("CVR", 7, 7, 7);
    private final String magic;
    private final int writeVersion;
    private final int minimumVersion;
    private final int maximumVersion;
    ArchiveFormat(String magic, int writeVersion, int minimumVersion, int maximumVersion) {
        this.magic = magic;
        this.writeVersion = writeVersion;
        this.minimumVersion = minimumVersion;
        this.maximumVersion = maximumVersion;
    }
    public String magic() { return magic; }
    public int version() { return writeVersion; }
    public int minimumVersion() { return minimumVersion; }
    public int maximumVersion() { return maximumVersion; }
    public boolean supports(int version) { return version >= minimumVersion && version <= maximumVersion; }
}
