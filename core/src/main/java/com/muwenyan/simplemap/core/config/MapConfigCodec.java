package com.muwenyan.simplemap.core.config;

import com.muwenyan.simplemap.core.model.ColorMode;
import com.muwenyan.simplemap.core.model.MapMode;
import java.util.LinkedHashMap;
import java.util.Map;

public final class MapConfigCodec {
    public static final int CURRENT_VERSION = 2;

    private MapConfigCodec() {
    }

    public static String encode(MapConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config");
        }
        return "version=" + CURRENT_VERSION + "\n"
                + "enabled=" + config.enabled() + "\n"
                + "mode=" + config.mode() + "\n"
                + "colorMode=" + config.colorMode() + "\n"
                + "renderDistance=" + config.renderDistance() + "\n"
                + "maxUploadBytes=" + config.maxUploadBytes() + "\n";
    }

    public static MapConfig decode(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("configuration is empty");
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : text.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int separator = trimmed.indexOf('=');
            if (separator <= 0 || separator == trimmed.length() - 1) {
                throw new IllegalArgumentException("invalid configuration line");
            }
            String key = trimmed.substring(0, separator).trim();
            String value = trimmed.substring(separator + 1).trim();
            if (values.put(key, value) != null) {
                throw new IllegalArgumentException("duplicate configuration key: " + key);
            }
        }
        int version = integer(values.remove("version"), "version");
        if (version < 1 || version > CURRENT_VERSION) {
            throw new IllegalArgumentException("unsupported configuration version: " + version);
        }
        if (version == 1) {
            migrateV1(values);
        }
        requireKeys(values, "enabled", "mode", "colorMode", "renderDistance", "maxUploadBytes");
        if (values.size() != 5) {
            throw new IllegalArgumentException("unknown configuration key");
        }
        return new MapConfig(
                bool(values.get("enabled")),
                enumValue(values.get("mode"), MapMode.class),
                enumValue(values.get("colorMode"), ColorMode.class),
                integer(values.get("renderDistance"), "renderDistance"),
                integer(values.get("maxUploadBytes"), "maxUploadBytes"));
    }

    private static void migrateV1(Map<String, String> values) {
        if (values.containsKey("distance") && !values.containsKey("renderDistance")) {
            values.put("renderDistance", values.remove("distance"));
        }
        if (values.containsKey("uploadBytes") && !values.containsKey("maxUploadBytes")) {
            values.put("maxUploadBytes", values.remove("uploadBytes"));
        }
    }

    private static void requireKeys(Map<String, String> values, String... keys) {
        for (String key : keys) {
            if (!values.containsKey(key)) {
                throw new IllegalArgumentException("missing configuration key: " + key);
            }
        }
    }

    private static int integer(String value, String key) {
        if (value == null) {
            throw new IllegalArgumentException("missing configuration key: " + key);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("invalid integer for " + key, exception);
        }
    }

    private static boolean bool(String value) {
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalArgumentException("invalid boolean");
        }
        return Boolean.parseBoolean(value);
    }

    private static <T extends Enum<T>> T enumValue(String value, Class<T> type) {
        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("invalid " + type.getSimpleName(), exception);
        }
    }
}
