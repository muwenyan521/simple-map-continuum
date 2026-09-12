package com.muwenyan.simplemap.core.book;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapBookCraftingTest {
    @Test
    void recognizesCopyAndMergeInputs() {
        MapBook book = new MapBook(UUID.randomUUID(), UUID.randomUUID());
        book.save(book.owner(), new com.muwenyan.simplemap.core.model.RegionPos(0, 0), new byte[]{1});
        MapBookItemState written = MapBookItemState.written(book, "Map Book");
        MapBookItemState empty = MapBookItemState.empty();
        assertTrue(MapBookCrafting.matchesCopy(List.of(written, empty)));
        assertEquals(written, MapBookCrafting.copy(written, empty));
        assertTrue(MapBookCrafting.matchesMerge(List.of(written, written)));
    }
}
