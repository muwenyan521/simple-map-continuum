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

    @Test
    void malformedRegionRemovesAbortedSession() {
        UUID owner = UUID.randomUUID();
        MapBook book = new MapBook(UUID.randomUUID(), owner);
        MapBookTransferService service = new MapBookTransferService(1024, 1000);
        UUID session = service.start(book, owner, MapBookTransferMode.LEARN, 0);
        assertThrows(com.muwenyan.simplemap.core.protocol.ProtocolException.class,
                () -> service.accept(session, new byte[]{1, 2, 3}, 1));
        assertEquals(0, service.activeCount());
    }

    @Test
    void requestStartChecksActorPermission() throws Exception {
        UUID owner = UUID.randomUUID();
        MapBookRuntime runtime = new MapBookRuntime(java.nio.file.Files.createTempDirectory("map-book-request"));
        var book = runtime.create(owner);
        var service = new MapBookTransferService(1024, 1000);
        var request = new com.muwenyan.simplemap.core.protocol.MapBookRequest(book.id(),
                com.muwenyan.simplemap.core.protocol.MapBookOperation.LEARN);
        assertThrows(SecurityException.class, () -> service.start(runtime, request, UUID.randomUUID(), 0));
    }
}
