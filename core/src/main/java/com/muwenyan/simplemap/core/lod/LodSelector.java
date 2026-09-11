package com.muwenyan.simplemap.core.lod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class LodSelector {
    private LodSelector() { }
    public static List<LodLevel> select(int visibleRadius, int maxPages, int maxLevel) {
        if (visibleRadius < 0 || maxPages < 1 || maxLevel < 0) throw new IllegalArgumentException();
        List<LodLevel> result = new ArrayList<>();
        int pages = 0;
        for (int level = 0; level <= maxLevel && pages < maxPages; level++) {
            LodLevel lod = LodLevel.of(level);
            int diameter = Math.max(1, (visibleRadius * 2 + lod.scale() - 1) / lod.scale());
            int needed = Math.multiplyExact(diameter, diameter);
            if (pages + needed <= maxPages || result.isEmpty()) {
                result.add(lod);
                pages += Math.min(needed, maxPages - pages);
            }
        }
        return result.stream().sorted(Comparator.comparingInt(LodLevel::level)).toList();
    }
}
