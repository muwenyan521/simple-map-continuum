package com.muwenyan.simplemap.core.book;

import java.util.UUID;
import com.muwenyan.simplemap.core.model.RegionPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapBookItemStateTest {
    @Test
    void emptyAndWrittenStatesCarryExpectedIdentity() {
        MapBookItemState empty = MapBookItemState.empty();
        assertEquals(MapBookStatus.EMPTY, empty.status());
        MapBook book = new MapBook(UUID.randomUUID(), UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> MapBookItemState.written(book, "Book"));
        book.save(book.owner(), new RegionPos(0, 0), new byte[]{1});
        assertEquals(book.id(), MapBookItemState.written(book, "Book").id().orElseThrow());
    }
}
