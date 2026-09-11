package com.muwenyan.simplemap.core.model;

import java.util.Objects;

public record DimensionId(String value) {
    public DimensionId {
        value = Objects.requireNonNull(value, "value").trim();
        if (value.isEmpty() || value.length() > 256 || value.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("invalid dimension id");
        }
    }
}
