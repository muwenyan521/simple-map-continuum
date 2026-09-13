package com.muwenyan.simplemap.core.protocol;

import java.util.Objects;
import java.util.UUID;

public record MapBookRequest(UUID bookId, MapBookOperation operation) {
    public MapBookRequest {
        Objects.requireNonNull(bookId, "bookId");
        Objects.requireNonNull(operation, "operation");
    }
}
