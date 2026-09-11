package com.muwenyan.simplemap.core.persistence;

import com.muwenyan.simplemap.core.map.MapCell;
import com.muwenyan.simplemap.core.map.MapRegion;
import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.ChunkPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class RegionArchiveCodec {
    private static final int MAX_DIMENSION_BYTES = 256;

    private RegionArchiveCodec() {
    }

    public static byte[] encode(MapRegion region, ArchiveFormat format) throws ArchiveException {
        if (region == null || format == null) {
            throw new IllegalArgumentException("region/format");
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(8192);
            DataOutputStream out = new DataOutputStream(bytes);
            byte[] dimension = region.dimension().value().getBytes(StandardCharsets.UTF_8);
            if (dimension.length > MAX_DIMENSION_BYTES) {
                throw new ArchiveException("dimension id exceeds limit");
            }
            out.writeShort(dimension.length);
            out.write(dimension);
            out.writeInt(region.origin().x());
            out.writeInt(region.origin().z());
            for (int z = 0; z < 32; z++) {
                for (int x = 0; x < 32; x++) {
                    MapCell cell = region.cell(x, z);
                    out.writeBoolean(cell != null);
                    if (cell != null) {
                        out.writeInt(cell.position().x());
                        out.writeInt(cell.position().y());
                        out.writeInt(cell.position().z());
                        out.writeInt(cell.colorArgb());
                        out.writeInt(cell.height());
                        out.writeBoolean(cell.fluid());
                        out.writeBoolean(cell.complete());
                    }
                }
            }
            out.flush();
            return ArchiveCodec.encode(format, bytes.toByteArray());
        } catch (IOException exception) {
            throw new ArchiveException("cannot encode region", exception);
        }
    }

    public static MapRegion decode(byte[] encoded, ArchiveFormat format) throws ArchiveException {
        byte[] payload = ArchiveCodec.decode(format, encoded);
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
            int dimensionLength = in.readUnsignedShort();
            if (dimensionLength == 0 || dimensionLength > MAX_DIMENSION_BYTES || dimensionLength > in.available()) {
                throw new ArchiveException("invalid dimension length");
            }
            String dimension = new String(in.readNBytes(dimensionLength), StandardCharsets.UTF_8);
            MapRegion region = new MapRegion(new DimensionId(dimension),
                    new ChunkPos(in.readInt(), in.readInt()), 32, 32);
            for (int z = 0; z < 32; z++) {
                for (int x = 0; x < 32; x++) {
                    if (!in.readBoolean()) {
                        continue;
                    }
                    MapCell cell = new MapCell(new BlockPos(in.readInt(), in.readInt(), in.readInt()),
                            in.readInt(), in.readInt(), in.readBoolean(), in.readBoolean());
                    region.apply(x, z, cell);
                }
            }
            if (in.available() != 0) {
                throw new ArchiveException("trailing region data");
            }
            return region;
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ArchiveException archiveException) {
                throw archiveException;
            }
            throw new ArchiveException("cannot decode region", exception);
        }
    }
}
