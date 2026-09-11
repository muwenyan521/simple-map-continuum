package com.muwenyan.simplemap.core.cave;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CaveProjection {
    private CaveProjection() {
    }

    public static Optional<CaveLayer> select(List<CaveLayer> layers, int playerY, CaveConfig config) {
        Objects.requireNonNull(layers, "layers");
        Objects.requireNonNull(config, "config");
        if (config.mode() == CaveMode.OFF || layers.isEmpty()) {
            return Optional.empty();
        }
        Comparator<CaveLayer> nearest = Comparator.comparingInt(layer -> Math.abs(playerY - layer.topY()));
        return switch (config.mode()) {
            case OFF -> Optional.empty();
            case ON -> layers.stream().max(Comparator.comparingInt(CaveLayer::topY)).map(layer -> shade(layer, config));
            case AUTO -> layers.stream().filter(layer -> layer.topY() <= playerY)
                    .min(nearest).or(() -> layers.stream().min(nearest)).map(layer -> shade(layer, config));
        };
    }

    public static List<CaveLayer> limitAndShade(List<CaveLayer> layers, int playerY, CaveConfig config) {
        Objects.requireNonNull(layers, "layers");
        Objects.requireNonNull(config, "config");
        if (config.mode() == CaveMode.OFF) {
            return List.of();
        }
        return layers.stream().sorted(Comparator.comparingInt(layer -> Math.abs(playerY - layer.topY())))
                .limit(config.maxLayers()).map(layer -> shade(layer, config)).toList();
    }

    private static CaveLayer shade(CaveLayer layer, CaveConfig config) {
        return new CaveLayer(layer.topY(), layer.floorY(), layer.shadedArgb(config.lightMode()),
                layer.light(), layer.fluid(), layer.emissive());
    }
}
