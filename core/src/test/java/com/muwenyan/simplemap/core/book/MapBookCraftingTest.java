package com.muwenyan.simplemap.core.book;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapBookCraftingTest {
    @Test
    void copyRequiresOneWrittenAndOneEmptyBook() {
        UUID id = UUID.randomUUID();
        MapBookItemState written = new MapBookItemState(MapBookStatus.WRITTEN, id, "Atlas");
        assertEquals(true, MapBookCrafting.matchesCopy(List.of(written, MapBookItemState.empty())));
        assertEquals(false, MapBookCrafting.matchesCopy(List.of(written, written)));
        assertThrows(IllegalArgumentException.class, () -> MapBookCrafting.copy(written, written, UUID.randomUUID()));
    }

    @Test
    void copyAlwaysAllocatesCallerSuppliedIdentity() {
        UUID source = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        MapBookItemState result = MapBookCrafting.copy(
                new MapBookItemState(MapBookStatus.WRITTEN, source, "Atlas"),
                MapBookItemState.empty(), target);
        assertEquals(target, result.id().orElseThrow());
        assertNotEquals(source, result.id().orElseThrow());
    }

    @Test
    void recognizesMergeInputsAndProducesWrittenState() {
        MapBook book = new MapBook(UUID.randomUUID(), UUID.randomUUID());
        book.save(book.owner(), new com.muwenyan.simplemap.core.model.RegionPos(0, 0), new byte[]{1});
        MapBookItemState written = MapBookItemState.written(book, "Map Book");
        assertEquals(true, MapBookCrafting.matchesMerge(List.of(written, written)));
        assertEquals(MapBookStatus.WRITTEN,
                MapBookCrafting.merge(List.of(written, written), UUID.randomUUID(), "Merged").status());
    }
}
