package com.muwenyan.simplemap.core.persistence;

import com.muwenyan.simplemap.core.cave.CaveSnapshot;
import com.muwenyan.simplemap.core.model.ChunkPos;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.CRC32;

public final class CaveRegionArchive {
    public static final int MAGIC = 0x43565231;
    public static final int VERSION = 7;
    private static final int RECORD_MAGIC = 0x54494c45;
    private static final int MAX_RECORD_BYTES = 4 * 1024 * 1024;

    private CaveRegionArchive() { }

    public static byte[] encode(Map<ChunkPos, CaveSnapshot> snapshots) throws ArchiveException {
        if (snapshots == null) throw new ArchiveException("snapshots is null");
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(snapshots.size());
            for (Map.Entry<ChunkPos, CaveSnapshot> entry : snapshots.entrySet()) {
                byte[] payload = CaveSnapshotCodec.encode(entry.getValue());
                if (payload.length > MAX_RECORD_BYTES) throw new ArchiveException("cave record too large");
                out.writeInt(RECORD_MAGIC);
                out.writeInt(entry.getKey().x());
                out.writeInt(entry.getKey().z());
                out.writeInt(payload.length);
                out.write(payload);
                CRC32 crc = new CRC32();
                crc.update(payload);
                out.writeInt((int) crc.getValue());
            }
            out.flush();
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new ArchiveException("cannot encode cave region", exception);
        }
    }

    public static Map<ChunkPos, CaveSnapshot> decode(byte[] encoded) throws ArchiveException {
        if (encoded == null || encoded.length < 12) throw new ArchiveException("invalid cave region");
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(encoded));
            if (in.readInt() != MAGIC || in.readInt() != VERSION) throw new ArchiveException("unsupported cave region");
            int count = in.readInt();
            if (count < 0 || count > 1024) throw new ArchiveException("invalid cave record count");
            Map<ChunkPos, CaveSnapshot> result = new LinkedHashMap<>();
            for (int i = 0; i < count; i++) {
                if (in.readInt() != RECORD_MAGIC) throw new ArchiveException("invalid cave record magic");
                ChunkPos key = new ChunkPos(in.readInt(), in.readInt());
                int length = in.readInt();
                if (length < 1 || length > MAX_RECORD_BYTES || length > in.available() - 4) throw new ArchiveException("invalid cave record length");
                byte[] payload = in.readNBytes(length);
                int expected = in.readInt();
                CRC32 crc = new CRC32();
                crc.update(payload);
                if ((int) crc.getValue() != expected) throw new ArchiveException("cave record CRC mismatch");
                CaveSnapshot snapshot = CaveSnapshotCodec.decode(payload);
                if (!snapshot.chunk().equals(key)) throw new ArchiveException("cave chunk key mismatch");
                result.put(key, snapshot);
            }
            if (in.available() != 0) throw new ArchiveException("trailing cave region data");
            return Map.copyOf(result);
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ArchiveException archiveException) throw archiveException;
            throw new ArchiveException("cannot decode cave region", exception);
        }
    }
}
