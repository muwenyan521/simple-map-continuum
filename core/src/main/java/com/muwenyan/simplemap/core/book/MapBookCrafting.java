package com.muwenyan.simplemap.core.book;

import java.util.List;
import java.util.Objects;

public final class MapBookCrafting {
    private MapBookCrafting() { }

    public static boolean matchesCopy(List<MapBookItemState> inputs) {
        Objects.requireNonNull(inputs, "inputs");
        return inputs.size() == 2 && inputs.stream().filter(s -> s.status() == MapBookStatus.WRITTEN).count() == 1
                && inputs.stream().filter(s -> s.status() == MapBookStatus.EMPTY).count() == 1;
    }

    public static MapBookItemState copy(MapBookItemState written, MapBookItemState empty) {
        if (written == null || empty == null || written.status() != MapBookStatus.WRITTEN
                || empty.status() != MapBookStatus.EMPTY) throw new IllegalArgumentException("copy requires written and empty books");
        return written;
    }

    public static boolean matchesMerge(List<MapBookItemState> inputs) {
        Objects.requireNonNull(inputs, "inputs");
        return inputs.size() == 2 && inputs.stream().allMatch(s -> s.status() == MapBookStatus.WRITTEN);
    }
}
