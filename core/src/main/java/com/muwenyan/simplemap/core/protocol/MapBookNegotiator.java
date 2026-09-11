package com.muwenyan.simplemap.core.protocol;

import java.util.LinkedHashSet;
import java.util.Set;

public final class MapBookNegotiator {
    private MapBookNegotiator() { }
    public static MapBookHello negotiate(MapBookHello local, MapBookHello remote) throws ProtocolException {
        if (local.protocolMajor() != remote.protocolMajor()) throw new ProtocolException(ProtocolErrorCode.UNSUPPORTED_VERSION, "protocol major mismatch");
        Set<String> formats = intersection(local.archiveFormats(), remote.archiveFormats());
        if (formats.isEmpty()) throw new ProtocolException(ProtocolErrorCode.INCOMPATIBLE_CAPABILITIES, "no shared archive format");
        return new MapBookHello(local.protocolMajor(), Math.min(local.protocolMinor(), remote.protocolMinor()), Math.min(local.maxArchiveBytes(), remote.maxArchiveBytes()), formats, intersection(local.capabilities(), remote.capabilities()));
    }
    private static Set<String> intersection(Set<String> left, Set<String> right) { LinkedHashSet<String> result = new LinkedHashSet<>(left); result.retainAll(right); return result; }
}
