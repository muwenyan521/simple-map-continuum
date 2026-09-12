package com.muwenyan.simplemap.core.book;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapBookItemCodecTest {
    @Test
    void roundTripsEmptyAndWrittenStates() throws Exception {
        MapBookItemState empty = MapBookItemState.empty();
        assertEquals(empty, MapBookItemCodec.decode(MapBookItemCodec.encode(empty)));

        MapBook book = new MapBook(UUID.randomUUID(), UUID.randomUUID());
        book.save(book.owner(), new com.muwenyan.simplemap.core.model.RegionPos(0, 0), new byte[]{1});
        MapBookItemState written = MapBookItemState.written(book, "我的地图册");
        assertEquals(written, MapBookItemCodec.decode(MapBookItemCodec.encode(written)));
    }

    @Test
    void rejectsUnknownVersionAndTrailingBytes() throws Exception {
        byte[] encoded = MapBookItemCodec.encode(MapBookItemState.empty());
        encoded[4] = 2;
        assertThrows(java.io.IOException.class, () -> MapBookItemCodec.decode(encoded));
        byte[] trailing = java.util.Arrays.copyOf(encoded, encoded.length + 1);
        trailing[4] = 1;
        assertThrows(java.io.IOException.class, () -> MapBookItemCodec.decode(trailing));
    }
}
