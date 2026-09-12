package com.muwenyan.simplemap.core.config;

import com.muwenyan.simplemap.core.cave.CaveConfig;
import com.muwenyan.simplemap.core.cave.CaveLightMode;
import com.muwenyan.simplemap.core.cave.CaveMode;
import com.muwenyan.simplemap.core.feature.MapFeatureFlags;
import com.muwenyan.simplemap.core.minimap.MinimapAnchor;
import com.muwenyan.simplemap.core.minimap.MinimapConfig;
import com.muwenyan.simplemap.core.minimap.MinimapShape;
import com.muwenyan.simplemap.core.style.MapStyle;
import com.muwenyan.simplemap.core.style.ReliefMode;
import com.muwenyan.simplemap.core.style.WaterShading;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class ClientMapConfigCodec {
    private ClientMapConfigCodec() { }

    public static String encode(ClientMapConfig config) {
        if (config == null) throw new IllegalArgumentException("config");
        StringBuilder output = new StringBuilder(MapConfigCodec.encode(config.map()));
        MinimapConfig minimap = config.minimap();
        output.append("minimap.enabled=").append(minimap.enabled()).append('\n')
                .append("minimap.size=").append(minimap.sizePixels()).append('\n')
                .append("minimap.zoom=").append(minimap.zoom()).append('\n')
                .append("minimap.shape=").append(minimap.shape()).append('\n')
                .append("minimap.anchor=").append(minimap.anchor()).append('\n')
                .append("minimap.rotate=").append(minimap.rotateWithPlayer()).append('\n')
                .append("minimap.coordinates=").append(minimap.showCoordinates()).append('\n')
                .append("cave.mode=").append(config.cave().mode()).append('\n')
                .append("cave.topY=").append(config.cave().topY()).append('\n')
                .append("cave.maxLayers=").append(config.cave().maxLayers()).append('\n')
                .append("cave.light=").append(config.cave().lightMode()).append('\n')
                .append("style.relief=").append(config.style().relief()).append('\n')
                .append("style.water=").append(config.style().water()).append('\n')
                .append("style.flowers=").append(config.style().flowers()).append('\n');
        appendFeatures(output, config.features());
        return output.toString();
    }

    public static ClientMapConfig decode(String encoded) {
        if (encoded == null || encoded.isBlank()) throw new IllegalArgumentException("configuration is empty");
        Map<String, String> values = parse(encoded);
        MapConfig map = MapConfigCodec.decode(MapConfigCodec.encode(new MapConfig(
                bool(values, "enabled"), enumValue(values, "mode", com.muwenyan.simplemap.core.model.MapMode.class),
                enumValue(values, "colorMode", com.muwenyan.simplemap.core.model.ColorMode.class),
                integer(values, "renderDistance"), integer(values, "maxUploadBytes"))));
        MinimapConfig minimap = new MinimapConfig(bool(values, "minimap.enabled"), integer(values, "minimap.size"),
                decimal(values, "minimap.zoom"), enumValue(values, "minimap.shape", MinimapShape.class),
                enumValue(values, "minimap.anchor", MinimapAnchor.class), bool(values, "minimap.rotate"),
                bool(values, "minimap.coordinates"));
        CaveConfig cave = new CaveConfig(enumValue(values, "cave.mode", CaveMode.class), integer(values, "cave.topY"),
                integer(values, "cave.maxLayers"), enumValue(values, "cave.light", CaveLightMode.class));
        MapStyle style = new MapStyle(com.muwenyan.simplemap.core.color.ColorProfile.BALANCED,
                enumValue(values, "style.relief", ReliefMode.class), enumValue(values, "style.water", WaterShading.class),
                bool(values, "style.flowers"), Map.of());
        MapFeatureFlags features = new MapFeatureFlags(bool(values, "feature.fullscreenMap"), bool(values, "feature.minimap"),
                bool(values, "feature.waypoints"), bool(values, "feature.blockInformation"), bool(values, "feature.biomeInformation"),
                bool(values, "feature.caveMap"), bool(values, "feature.terrainRelief"), bool(values, "feature.waterShading"),
                bool(values, "feature.flowers"), bool(values, "feature.mapBook"), bool(values, "feature.debugOverlay"));
        return new ClientMapConfig(map, minimap, cave, style, features);
    }

    private static void appendFeatures(StringBuilder output, MapFeatureFlags flags) {
        output.append("feature.fullscreenMap=").append(flags.fullscreenMap()).append('\n')
                .append("feature.minimap=").append(flags.minimap()).append('\n')
                .append("feature.waypoints=").append(flags.waypoints()).append('\n')
                .append("feature.blockInformation=").append(flags.blockInformation()).append('\n')
                .append("feature.biomeInformation=").append(flags.biomeInformation()).append('\n')
                .append("feature.caveMap=").append(flags.caveMap()).append('\n')
                .append("feature.terrainRelief=").append(flags.terrainRelief()).append('\n')
                .append("feature.waterShading=").append(flags.waterShading()).append('\n')
                .append("feature.flowers=").append(flags.flowers()).append('\n')
                .append("feature.mapBook=").append(flags.mapBook()).append('\n')
                .append("feature.debugOverlay=").append(flags.debugOverlay()).append('\n');
    }

    private static Map<String, String> parse(String encoded) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : encoded.split("\\R")) {
            if (line.isBlank()) continue;
            int separator = line.indexOf('=');
            if (separator <= 0 || values.put(line.substring(0, separator), line.substring(separator + 1)) != null) {
                throw new IllegalArgumentException("invalid or duplicate configuration key");
            }
        }
        return values;
    }

    private static String require(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null) throw new IllegalArgumentException("missing configuration key: " + key);
        return value;
    }

    private static boolean bool(Map<String, String> values, String key) {
        String value = require(values, key);
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) throw new IllegalArgumentException("invalid boolean");
        return Boolean.parseBoolean(value);
    }

    private static int integer(Map<String, String> values, String key) {
        try { return Integer.parseInt(require(values, key)); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("invalid integer: " + key, exception); }
    }

    private static double decimal(Map<String, String> values, String key) {
        try { return Double.parseDouble(require(values, key)); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("invalid decimal: " + key, exception); }
    }

    private static <T extends Enum<T>> T enumValue(Map<String, String> values, String key, Class<T> type) {
        try { return Enum.valueOf(type, require(values, key).toUpperCase(Locale.ROOT)); }
        catch (RuntimeException exception) { throw new IllegalArgumentException("invalid enum: " + key, exception); }
    }
}
