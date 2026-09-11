package com.muwenyan.simplemap.core.protocol;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public record MapBookHello(int protocolMajor, int protocolMinor, int maxArchiveBytes,
                           Set<String> archiveFormats, Set<String> capabilities) {
    public MapBookHello {
        if (protocolMajor < 0 || protocolMinor < 0) throw new IllegalArgumentException("negative protocol version");
        if (maxArchiveBytes <= 0 || maxArchiveBytes > FrameCodec.MAX_ARCHIVE_BYTES) throw new IllegalArgumentException("archive limit out of range");
        archiveFormats = immutableNames(archiveFormats, "archive format");
        capabilities = immutableNames(capabilities, "capability");
    }
    private static Set<String> immutableNames(Set<String> values, String label) {
        Objects.requireNonNull(values, label);
        LinkedHashSet<String> copy = new LinkedHashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank() || value.length() > 64) throw new IllegalArgumentException("invalid " + label);
            copy.add(value);
        }
        return Collections.unmodifiableSet(copy);
    }
}
