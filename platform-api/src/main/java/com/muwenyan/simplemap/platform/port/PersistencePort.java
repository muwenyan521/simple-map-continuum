package com.muwenyan.simplemap.platform.port;

import com.muwenyan.simplemap.core.model.CaveTile;
import com.muwenyan.simplemap.core.model.TileKey;
import java.util.Optional;

public interface PersistencePort {
    Optional<CaveTile> read(TileKey key);
    void write(CaveTile tile);
}
