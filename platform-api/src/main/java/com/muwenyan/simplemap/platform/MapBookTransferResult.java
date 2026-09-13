package com.muwenyan.simplemap.platform;

public record MapBookTransferResult(MapBookTransferState state, int acceptedRegions) {
    public MapBookTransferResult {
        if (state == null || acceptedRegions < 0) {
            throw new IllegalArgumentException("invalid transfer result");
        }
    }
}
