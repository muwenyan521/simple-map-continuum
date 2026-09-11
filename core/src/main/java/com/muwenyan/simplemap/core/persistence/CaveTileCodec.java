package com.muwenyan.simplemap.core.persistence;

import com.muwenyan.simplemap.core.model.CaveTile;
import com.muwenyan.simplemap.core.model.DimensionId;
import com.muwenyan.simplemap.core.model.RegionPos;
import com.muwenyan.simplemap.core.model.TileKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class CaveTileCodec {
    private static final int MAX_DIMENSION_BYTES = 256;
    private static final int MAX_PIXELS = 4 * 1024 * 1024;

    private CaveTileCodec() { }

    public static byte[] encode(CaveTile tile) throws ArchiveException {
        if (tile == null || tile.pixels().length > MAX_PIXELS) throw new ArchiveException("invalid cave tile");
        try {
            ByteArrayOutputStream payload = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(payload);
            byte[] dimension = tile.key().dimension().value().getBytes(StandardCharsets.UTF_8);
            if (dimension.length > MAX_DIMENSION_BYTES) throw new ArchiveException("dimension too long");
            out.writeShort(dimension.length); out.write(dimension);
            out.writeInt(tile.key().region().x()); out.writeInt(tile.key().region().z());
            out.writeInt(tile.key().lod()); out.writeInt(tile.key().x()); out.writeInt(tile.key().z());
            out.writeLong(tile.epoch());
            byte[] pixels = tile.pixels(); out.writeInt(pixels.length); out.write(pixels); out.flush();
            return ArchiveCodec.encode(ArchiveFormat.CVR, payload.toByteArray());
        } catch (IOException exception) {
            throw new ArchiveException("cannot encode cave tile", exception);
        }
    }

    public static CaveTile decode(byte[] encoded) throws ArchiveException {
        byte[] payload = ArchiveCodec.decode(ArchiveFormat.CVR, encoded);
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(payload));
            int dimensionLength = in.readUnsignedShort();
            if (dimensionLength < 1 || dimensionLength > MAX_DIMENSION_BYTES || dimensionLength > in.available()) {
                throw new ArchiveException("invalid dimension length");
            }
            DimensionId dimension = new DimensionId(new String(in.readNBytes(dimensionLength), StandardCharsets.UTF_8));
            TileKey key = new TileKey(dimension, new RegionPos(in.readInt(), in.readInt()),
                    in.readInt(), in.readInt(), in.readInt());
            long epoch = in.readLong();
            int length = in.readInt();
            if (length < 1 || length > MAX_PIXELS || length > in.available()) throw new ArchiveException("invalid pixel length");
            byte[] pixels = in.readNBytes(length);
            if (in.available() != 0) throw new ArchiveException("trailing cave tile data");
            return new CaveTile(key, epoch, pixels);
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ArchiveException archiveException) throw archiveException;
            throw new ArchiveException("cannot decode cave tile", exception);
        }
    }
}
