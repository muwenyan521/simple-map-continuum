package com.muwenyan.simplemap.core.protocol;

public record ProtocolVersion(int major, int minor) {
    public ProtocolVersion { if (major < 0 || minor < 0) throw new IllegalArgumentException("protocol version"); }
    public boolean compatibleWith(ProtocolVersion other) { return major == other.major; }
}
