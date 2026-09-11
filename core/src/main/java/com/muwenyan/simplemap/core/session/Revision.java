package com.muwenyan.simplemap.core.session;

public record Revision(long value) {
    public Revision { if (value < 0) throw new IllegalArgumentException("revision"); }
    public Revision next() { return new Revision(Math.addExact(value, 1)); }
}
