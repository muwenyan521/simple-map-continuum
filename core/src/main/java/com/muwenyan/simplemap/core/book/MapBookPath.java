package com.muwenyan.simplemap.core.book;

import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.model.RegionPos;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public final class MapBookPath {
    private MapBookPath() { }

    public static String dimensionFolder(DimensionId dimension) {
        Objects.requireNonNull(dimension, "dimension");
        String value = dimension.value();
        String folder = value.replace(':', '_').replace('/', '_').replace('\\', '_');
        if (folder.isBlank() || folder.equals(".") || folder.equals("..") || folder.contains("..")) {
            throw new IllegalArgumentException("unsafe dimension folder");
        }
        return folder;
    }

    public static Path region(Path root, UUID bookId, DimensionId dimension, RegionPos region) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(bookId, "bookId");
        Objects.requireNonNull(region, "region");
        return root.resolve("simplemap_books").resolve(bookId.toString())
                .resolve(dimensionFolder(dimension))
                .resolve("r." + region.x() + "." + region.z() + ".smdat");
    }
}
