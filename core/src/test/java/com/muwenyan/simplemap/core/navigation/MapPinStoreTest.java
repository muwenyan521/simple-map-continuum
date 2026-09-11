package com.muwenyan.simplemap.core.navigation;

import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapPinStoreTest {
    @Test
    void filtersPinsByDimensionAndSortsLabels() {
        MapPinStore store = new MapPinStore();
        DimensionId overworld = new DimensionId("minecraft:overworld");
        store.upsert(new MapPin(UUID.randomUUID(), overworld, new BlockPos(0, 64, 0), "Zoo", 0xffff0000));
        store.upsert(new MapPin(UUID.randomUUID(), overworld, new BlockPos(1, 64, 1), "Base", 0xff00ff00));
        store.upsert(new MapPin(UUID.randomUUID(), new DimensionId("minecraft:the_nether"), new BlockPos(1, 64, 1), "Gate", 0xff0000ff));
        assertEquals("Base", store.visible(overworld).get(0).label());
        assertEquals(2, store.visible(overworld).size());
    }
}
