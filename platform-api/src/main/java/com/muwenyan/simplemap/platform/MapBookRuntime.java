package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.book.MapBook;
import com.muwenyan.simplemap.core.book.MapBookItemState;
import com.muwenyan.simplemap.core.book.MapBookStatus;
import com.muwenyan.simplemap.core.book.MapBookCrafting;
import com.muwenyan.simplemap.core.book.BookPermission;
import com.muwenyan.simplemap.platform.file.MapBookFileService;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public final class MapBookRuntime {
    private final MapBookFileService files;

    public MapBookRuntime(Path root) { files = new MapBookFileService(Objects.requireNonNull(root, "root")); }

    public synchronized MapBook create(UUID owner) throws IOException {
        MapBook book = new MapBook(UUID.randomUUID(), Objects.requireNonNull(owner, "owner"));
        files.write(book);
        return book;
    }

    public synchronized MapBook load(UUID id) throws IOException { return files.readRecovering(Objects.requireNonNull(id, "id")); }

    public synchronized void save(MapBook book) throws IOException { files.write(Objects.requireNonNull(book, "book")); }

    public synchronized MapBook grant(UUID bookId, UUID actor, UUID subject, BookPermission permission) throws IOException {
        MapBook book = load(bookId);
        book.grant(actor, subject, permission);
        save(book);
        return book;
    }

    public synchronized MapBook revoke(UUID bookId, UUID actor, UUID subject) throws IOException {
        MapBook book = load(bookId);
        book.revoke(actor, subject);
        save(book);
        return book;
    }

    public synchronized MapBook copy(MapBook source, UUID actor, UUID owner) throws IOException {
        Objects.requireNonNull(source, "source");
        MapBook copy = source.copy(Objects.requireNonNull(actor, "actor"), UUID.randomUUID(), Objects.requireNonNull(owner, "owner"));
        files.write(copy);
        return copy;
    }

    public synchronized MapBook merge(MapBook target, MapBook source, UUID actor) throws IOException {
        target.merge(Objects.requireNonNull(actor, "actor"), Objects.requireNonNull(source, "source"));
        files.write(target);
        return target;
    }

    public synchronized MapBookItemState copyItem(MapBookItemState written, MapBookItemState empty,
                                                   UUID actor, UUID owner) throws IOException {
        if (!MapBookCrafting.matchesCopy(java.util.List.of(written, empty))) {
            throw new IllegalArgumentException("invalid map book copy inputs");
        }
        MapBook source = load(written.id().orElseThrow());
        MapBook copy = copy(source, actor, owner);
        return MapBookItemState.written(copy, written.title());
    }

    public synchronized MapBookItemState mergeItems(MapBookItemState left, MapBookItemState right,
                                                     UUID actor, String title) throws IOException {
        if (!MapBookCrafting.matchesMerge(java.util.List.of(left, right))) {
            throw new IllegalArgumentException("invalid map book merge inputs");
        }
        MapBook target = load(left.id().orElseThrow());
        MapBook source = load(right.id().orElseThrow());
        MapBook merged = target.copy(actor, UUID.randomUUID(), actor);
        merge(merged, source, actor);
        return MapBookItemState.written(merged, title);
    }

    public static MapBookItemState emptyItem() { return MapBookItemState.empty(); }
}
