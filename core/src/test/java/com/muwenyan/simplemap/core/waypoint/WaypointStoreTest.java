package com.muwenyan.simplemap.core.waypoint;

import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.navigation.Waypoint;
import java.util.UUID;

public final class WaypointStoreTest {
    private WaypointStoreTest() { }
    public static void main(String[] args) {
        DimensionId dim = new DimensionId("minecraft:overworld");
        WaypointStore store = new WaypointStore();
        UUID id = UUID.randomUUID();
        store.upsert(new Waypoint(id, dim, new BlockPos(0, 64, 0), "zeta", true));
        store.upsert(new Waypoint(UUID.randomUUID(), dim, new BlockPos(0, 64, 0), "alpha", true));
        if (store.visible(dim).get(0).name().equals("zeta")) throw new AssertionError("sort");
        if (!store.remove(id) || store.all().size() != 1) throw new AssertionError("remove");
        UUID followId = store.all().get(0).id();
        store.follow(followId, dim);
        if (store.followed().isEmpty()) throw new AssertionError("follow");
        store.remove(followId);
        if (store.followed().isPresent()) throw new AssertionError("follow clear");
        System.out.println("WAYPOINT_STORE_PASS");
    }
}
