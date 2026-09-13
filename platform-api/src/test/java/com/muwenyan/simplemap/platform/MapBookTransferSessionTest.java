package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.core.book.MapBook;
import com.muwenyan.simplemap.core.book.MapBookStatus;
import com.muwenyan.simplemap.core.book.MapBookRegion;
import com.muwenyan.simplemap.core.model.RegionPos;
import com.muwenyan.simplemap.core.protocol.MapBookRegionCodec;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapBookTransferSessionTest {
    @Test
    void saveSessionAcceptsRegionsAndCompletes() throws Exception {
        UUID owner = UUID.randomUUID();
        MapBook book = new MapBook(UUID.randomUUID(), owner);
        MapBookTransferSession session = new MapBookTransferSession(book, owner,
                MapBookTransferMode.SAVE, 1024, 100, 1000);
        byte[] wire = MapBookRegionCodec.encode(new MapBookRegion(new RegionPos(1, 2), 1, new byte[]{4}));
        assertEquals(MapBookTransferState.ACTIVE, session.acceptRegion(wire, 200).state());
        assertEquals(MapBookTransferState.COMPLETED, session.complete(300).state());
        assertEquals(1, book.snapshot().regions().size());
        assertEquals(MapBookStatus.WRITTEN, book.status());
    }

    @Test
    void learnSessionCommitsOnlyOnCompletion() throws Exception {
        UUID owner = UUID.randomUUID();
        MapBook book = new MapBook(UUID.randomUUID(), owner);
        MapBookTransferSession session = new MapBookTransferSession(book, owner,
                MapBookTransferMode.LEARN, 1024, 0, 1000);
        byte[] wire = MapBookRegionCodec.encode(new MapBookRegion(new RegionPos(0, 0), 1, new byte[]{8}));
        session.acceptRegion(wire, 1);
        assertEquals(0, book.snapshot().regions().size());
        session.complete(2);
        assertEquals(1, book.snapshot().regions().size());
    }

    @Test
    void learnSessionTimeoutAbortsStagedRegions() throws Exception {
        UUID owner = UUID.randomUUID();
        MapBook book = new MapBook(UUID.randomUUID(), owner);
        MapBookTransferSession session = new MapBookTransferSession(book, owner,
                MapBookTransferMode.LEARN, 1024, 0, 10);
        byte[] wire = MapBookRegionCodec.encode(new MapBookRegion(new RegionPos(0, 0), 1, new byte[]{8}));
        session.acceptRegion(wire, 1);
        assertEquals(MapBookTransferState.TIMED_OUT, session.tick(11).state());
        assertEquals(0, book.snapshot().regions().size());
        assertThrows(IllegalStateException.class, () -> session.complete(12));
    }

    @Test
    void malformedOrOversizedRegionFailsWithoutCommittingLearnSession() {
        UUID owner = UUID.randomUUID();
        MapBook book = new MapBook(UUID.randomUUID(), owner);
        MapBookTransferSession session = new MapBookTransferSession(book, owner,
                MapBookTransferMode.LEARN, 4, 0, 1000);
        assertThrows(com.muwenyan.simplemap.core.protocol.ProtocolException.class,
                () -> session.acceptRegion(new byte[]{1, 2, 3, 4, 5}, 1));
        assertEquals(MapBookStatus.EMPTY, book.status());
    }
}
