package com.muwenyan.simplemap.core.book;

import com.muwenyan.simplemap.core.model.RegionPos;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapBookTest {
    private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID WRITER = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID READER = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000004");

    @Test
    void permissionsLearningAndSnapshot() {
        MapBook book = new MapBook(UUID.randomUUID(), OWNER);
        book.grant(OWNER, WRITER, BookPermission.WRITE);
        book.grant(OWNER, READER, BookPermission.READ);
        assertThrows(SecurityException.class, () -> book.save(READER, new RegionPos(0, 0), new byte[]{1}));
        try (MapBook.LearningSession session = book.beginLearning(WRITER)) {
            session.accept(new RegionPos(0, 0), new byte[]{1, 2});
            session.accept(new RegionPos(1, 0), new byte[]{3});
            assertEquals(2, session.stagedCount());
            session.commit();
        }
        assertEquals(MapBookStatus.WRITTEN, book.status());
        assertEquals(2, book.snapshot().regions().size());
    }

    @Test
    void abortedLearningDoesNotPublish() {
        MapBook book = new MapBook(UUID.randomUUID(), OWNER);
        try (MapBook.LearningSession session = book.beginLearning(OWNER)) {
            session.accept(new RegionPos(0, 0), new byte[]{1});
        }
        assertEquals(MapBookStatus.EMPTY, book.status());
        assertEquals(0, book.snapshot().regions().size());
    }

    @Test
    void mergeUsesNewestRegionRevisionAndCopyIsReadable() {
        MapBook first = new MapBook(UUID.randomUUID(), OWNER);
        MapBook second = new MapBook(UUID.randomUUID(), OWNER);
        first.save(OWNER, new RegionPos(0, 0), new byte[]{1});
        second.save(OWNER, new RegionPos(0, 0), new byte[]{2});
        second.save(OWNER, new RegionPos(1, 0), new byte[]{3});
        first.merge(OWNER, second);
        assertEquals(2, first.snapshot().regions().size());
        MapBook copy = first.copy(OWNER, UUID.randomUUID(), READER);
        assertEquals(2, copy.snapshot().regions().size());
        assertThrows(SecurityException.class, () -> copy.save(OTHER, new RegionPos(2, 0), new byte[]{4}));
    }
}
