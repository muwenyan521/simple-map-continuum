package com.muwenyan.simplemap.core.protocol;

import java.util.Set;

public record Handshake(ProtocolVersion protocol, Set<String> archiveFormats, int maxArchiveBytes, Set<Capability> capabilities) {
    public Handshake {
        if (protocol == null || archiveFormats == null || capabilities == null) throw new NullPointerException();
        if (maxArchiveBytes < 1024) throw new IllegalArgumentException("max archive bytes");
        archiveFormats = Set.copyOf(archiveFormats);
        capabilities = Set.copyOf(capabilities);
    }
}
