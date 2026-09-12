package com.muwenyan.simplemap.core.persistence;

import java.util.Map;

public record Smr2ScanResult(Map<Smr2Record.RecordKey, Smr2Record> latest,
                             long validBytes, boolean damagedTail) {
    public Smr2ScanResult {
        latest = Map.copyOf(latest);
        if (validBytes < 0) throw new IllegalArgumentException("valid bytes must be non-negative");
    }
}
