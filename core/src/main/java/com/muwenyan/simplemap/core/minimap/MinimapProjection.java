package com.muwenyan.simplemap.core.minimap;

import com.muwenyan.simplemap.core.model.ChunkPos;

public final class MinimapProjection {
    private MinimapProjection() {
    }

    public static ScreenPoint worldToScreen(
            double worldX,
            double worldZ,
            double centerX,
            double centerZ,
            double zoom,
            double playerYawDegrees,
            boolean rotateWithPlayer,
            int sizePixels) {
        validate(zoom, sizePixels);
        double dx = (worldX - centerX) * zoom;
        double dz = (worldZ - centerZ) * zoom;
        if (rotateWithPlayer) {
            double radians = Math.toRadians(-playerYawDegrees);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double rotatedX = dx * cos - dz * sin;
            double rotatedZ = dx * sin + dz * cos;
            dx = rotatedX;
            dz = rotatedZ;
        }
        double half = sizePixels / 2d;
        return new ScreenPoint(half + dx, half + dz);
    }

    public static boolean visible(ScreenPoint point, int sizePixels, MinimapShape shape) {
        if (point == null || shape == null || sizePixels <= 0) {
            throw new IllegalArgumentException("point/shape/size");
        }
        double half = sizePixels / 2d;
        double x = point.x() - half;
        double z = point.z() - half;
        return switch (shape) {
            case SQUARE -> Math.abs(x) <= half && Math.abs(z) <= half;
            case CIRCLE -> x * x + z * z <= half * half;
        };
    }

    public static ScreenPoint chunkCenterToScreen(
            ChunkPos chunk,
            double centerX,
            double centerZ,
            double zoom,
            double playerYawDegrees,
            boolean rotateWithPlayer,
            int sizePixels) {
        return worldToScreen(chunk.x() * 16d + 8d, chunk.z() * 16d + 8d,
                centerX, centerZ, zoom, playerYawDegrees, rotateWithPlayer, sizePixels);
    }

    private static void validate(double zoom, int sizePixels) {
        if (!Double.isFinite(zoom) || zoom <= 0d || sizePixels <= 0) {
            throw new IllegalArgumentException("invalid projection parameters");
        }
    }

    public record ScreenPoint(double x, double z) {
        public ScreenPoint {
            if (!Double.isFinite(x) || !Double.isFinite(z)) {
                throw new IllegalArgumentException("screen coordinates must be finite");
            }
        }
    }
}
