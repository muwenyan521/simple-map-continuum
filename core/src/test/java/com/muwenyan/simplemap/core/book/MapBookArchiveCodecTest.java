package com.muwenyan.simplemap.core.book;

import com.muwenyan.simplemap.core.model.RegionPos;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MapBookArchiveCodecTest {
    @Test
    void archiveRoundTripPreservesIdentityAndRegionRevisions() throws Exception {
        UUID owner = UUID.randomUUID();
        MapBook book = new MapBook(UUID.randomUUID(), owner);
        book.save(owner, new RegionPos(-1, 2), new byte[]{1, 2, 3});
        MapBook decoded = MapBookArchiveCodec.decode(MapBookArchiveCodec.encode(book));
        assertEquals(book.id(), decoded.id());
        assertEquals(book.owner(), decoded.owner());
        assertEquals(book.snapshot().revision(), decoded.snapshot().revision());
        assertArrayEquals(book.snapshot().regions().get(0).payload(), decoded.snapshot().regions().get(0).payload());
    }
}
