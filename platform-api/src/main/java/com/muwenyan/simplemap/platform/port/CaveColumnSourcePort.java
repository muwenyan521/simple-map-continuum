package com.muwenyan.simplemap.platform.port;

import com.muwenyan.simplemap.core.cave.CaveColumnRun;
import com.muwenyan.simplemap.core.cave.CaveConfig;
import com.muwenyan.simplemap.core.model.ChunkPos;
import java.util.List;

@FunctionalInterface
public interface CaveColumnSourcePort {
    List<CaveColumnRun> sample(ChunkPos chunk, int localX, int localZ, CaveConfig config);
}
