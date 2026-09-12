package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.book.MapBook;
import com.muwenyan.simplemap.core.book.MapBookRegion;
import com.muwenyan.simplemap.core.model.RegionPos;
import com.muwenyan.simplemap.core.protocol.MapBookRegionCodec;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapBookServiceTest {
    @Test
    void decodesRegionPayloadIntoBookState() throws Exception {
        UUID owner = UUID.randomUUID();
        MapBook book = new MapBook(UUID.randomUUID(), owner);
        MapBookService service = new MapBookService(book, 1024);
        service.save(owner, MapBookRegionCodec.encode(new MapBookRegion(new RegionPos(1, 2), 1, new byte[]{1})));
        assertEquals(1, book.snapshot().regions().size());
    }
}
