package com.muwenyan.simplemap.core.server;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class ServerMapConfigCodec {
    public static final int CURRENT_VERSION = 1;

    private ServerMapConfigCodec() {
    }

    public static String encode(ServerMapConfig config) {
        if (config == null) throw new IllegalArgumentException("config");
        return "version=1\n" + "enabled=" + config.enabled() + "\n"
                + "allowMapBooks=" + config.allowMapBooks() + "\n"
                + "allowWaypointSync=" + config.allowWaypointSync() + "\n"
                + "maxRegionsPerBook=" + config.maxRegionsPerBook() + "\n"
                + "maxPayloadBytes=" + config.maxPayloadBytes() + "\n";
    }

    public static ServerMapConfig decode(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("configuration is empty");
        Map<String, String> values = new LinkedHashMap<>();
        for (String line : text.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            int separator = trimmed.indexOf('=');
            if (separator <= 0 || separator == trimmed.length() - 1) throw new IllegalArgumentException("invalid line");
            if (values.put(trimmed.substring(0, separator).trim(), trimmed.substring(separator + 1).trim()) != null) {
                throw new IllegalArgumentException("duplicate key");
            }
        }
        int version = integer(values.remove("version"));
        if (version != CURRENT_VERSION) throw new IllegalArgumentException("unsupported version: " + version);
        if (values.size() != 5 || !values.keySet().containsAll(java.util.Set.of("enabled", "allowMapBooks",
                "allowWaypointSync", "maxRegionsPerBook", "maxPayloadBytes"))) {
            throw new IllegalArgumentException("missing or unknown key");
        }
        return new ServerMapConfig(bool(values.get("enabled")), bool(values.get("allowMapBooks")),
                bool(values.get("allowWaypointSync")), integer(values.get("maxRegionsPerBook")),
                integer(values.get("maxPayloadBytes")));
    }

    private static int integer(String value) {
        try { return Integer.parseInt(value); }
        catch (RuntimeException exception) { throw new IllegalArgumentException("invalid integer", exception); }
    }

    private static boolean bool(String value) {
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalArgumentException("invalid boolean");
        }
        return Boolean.parseBoolean(value.toLowerCase(Locale.ROOT));
    }
}
