package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.book.MapBook;
import com.muwenyan.simplemap.core.book.MapBookRegion;
import com.muwenyan.simplemap.core.model.RegionPos;
import com.muwenyan.simplemap.core.protocol.MapBookRegionCodec;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapBookTransferServiceTest {
    @Test
    void serviceOwnsSessionLifecycleAndRemovesCompletedSession() throws Exception {
        UUID owner = UUID.randomUUID();
        MapBook book = new MapBook(UUID.randomUUID(), owner);
        MapBookTransferService service = new MapBookTransferService(1024, 1000);
        UUID session = service.start(book, owner, MapBookTransferMode.SAVE, 0);
        byte[] region = MapBookRegionCodec.encode(new MapBookRegion(new RegionPos(0, 0), 1, new byte[]{1}));
        assertEquals(MapBookTransferState.ACTIVE, service.accept(session, region, 1).state());
        assertEquals(MapBookTransferState.COMPLETED, service.complete(session, 2).state());
        assertEquals(0, service.activeCount());
        assertThrows(NullPointerException.class, () -> service.abort(session));
    }

    @Test
    void timeoutRemovesSessionWithoutCommittingLearnedData() throws Exception {
        UUID owner = UUID.randomUUID();
        MapBook book = new MapBook(UUID.randomUUID(), owner);
        MapBookTransferService service = new MapBookTransferService(1024, 10);
        UUID session = service.start(book, owner, MapBookTransferMode.LEARN, 0);
        byte[] region = MapBookRegionCodec.encode(new MapBookRegion(new RegionPos(0, 0), 1, new byte[]{1}));
        service.accept(session, region, 1);
        assertEquals(MapBookTransferState.TIMED_OUT, service.tick(session, 11).state());
        assertEquals(0, service.activeCount());
        assertEquals(0, book.snapshot().regions().size());
    }
}
