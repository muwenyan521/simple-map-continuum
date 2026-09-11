package com.muwenyan.simplemap.core.book;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record MapBookItemState(MapBookStatus status, UUID bookId, String title) {
    public MapBookItemState {
        status = Objects.requireNonNull(status, "status");
        title = Objects.requireNonNull(title, "title").trim();
        if (title.isEmpty() || title.length() > 128) {
            throw new IllegalArgumentException("invalid title");
        }
        if (status == MapBookStatus.EMPTY && bookId != null) {
            throw new IllegalArgumentException("empty book cannot have an id");
        }
        if (status != MapBookStatus.EMPTY && bookId == null) {
            throw new IllegalArgumentException("written book requires an id");
        }
    }

    public static MapBookItemState empty() {
        return new MapBookItemState(MapBookStatus.EMPTY, null, "Map Book");
    }

    public static MapBookItemState written(MapBook book, String title) {
        Objects.requireNonNull(book, "book");
        if (book.status() == MapBookStatus.EMPTY) throw new IllegalArgumentException("book has no regions");
        return new MapBookItemState(MapBookStatus.WRITTEN, book.id(), title);
    }

    public Optional<UUID> id() {
        return Optional.ofNullable(bookId);
    }
}
