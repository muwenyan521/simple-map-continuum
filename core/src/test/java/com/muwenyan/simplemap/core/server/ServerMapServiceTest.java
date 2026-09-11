package com.muwenyan.simplemap.core.server;

import com.muwenyan.simplemap.core.book.BookPermission;
import com.muwenyan.simplemap.core.book.MapBook;
import com.muwenyan.simplemap.core.model.RegionPos;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerMapServiceTest {
    @Test
    void enforcesServerLimitsAndBookPermissions() {
        UUID owner = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        MapBook book = new MapBook(UUID.randomUUID(), owner);
        book.grant(owner, player, BookPermission.WRITE);
        ServerMapService service = new ServerMapService(ServerMapConfig.defaults(),
                new ServerAccessPolicy(Set.of(), Set.of(ServerPermission.MAP_SAVE)));
        service.saveRegion(player, book, new RegionPos(0, 0), new byte[]{1});
        assertEquals(1, book.snapshot().regions().size());
        assertThrows(SecurityException.class, () -> service.saveRegion(UUID.randomUUID(), book,
                new RegionPos(1, 0), new byte[]{1}));
    }
}
