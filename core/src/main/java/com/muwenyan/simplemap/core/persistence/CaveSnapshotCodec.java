package com.muwenyan.simplemap.core.persistence;

import com.muwenyan.simplemap.core.cave.CaveColumnRun;
import com.muwenyan.simplemap.core.cave.CaveSnapshot;
import com.muwenyan.simplemap.core.model.ChunkPos;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class CaveSnapshotCodec {
    public static final int MAGIC = 0x43565434;
    public static final int VERSION = 10;
    private static final int MAX_BYTES = 4 * 1024 * 1024;
    private static final int MAX_RUNS_PER_COLUMN = 4096;

    private CaveSnapshotCodec() { }

    public static byte[] encode(CaveSnapshot snapshot) throws ArchiveException {
        if (snapshot == null) throw new ArchiveException("snapshot is null");
        try {
            ByteArrayOutputStream bodyBytes = new ByteArrayOutputStream();
            DataOutputStream body = new DataOutputStream(bodyBytes);
            body.writeInt(snapshot.chunk().x());
            body.writeInt(snapshot.chunk().z());
            body.writeLong(snapshot.revision());
            body.writeBoolean(snapshot.scanned());
            body.writeBoolean(snapshot.fullHeight());
            for (List<CaveColumnRun> column : snapshot.columns()) {
                if (column.size() > MAX_RUNS_PER_COLUMN) throw new ArchiveException("too many cave runs");
                body.writeInt(column.size());
                for (CaveColumnRun run : column) {
                    body.writeInt(run.topY());
                    body.writeInt(run.floorY());
                    body.writeInt(run.argb());
                    body.writeByte(run.light());
                    body.writeByte((run.fluid() ? 1 : 0) | (run.emissive() ? 2 : 0));
                }
            }
            body.flush();
            byte[] plain = bodyBytes.toByteArray();
            CRC32 crc = new CRC32();
            crc.update(plain);
            ByteArrayOutputStream compressedBytes = new ByteArrayOutputStream(plain.length / 2 + 32);
            try (GZIPOutputStream gzip = new GZIPOutputStream(compressedBytes)) {
                gzip.write(plain);
            }
            ByteArrayOutputStream result = new ByteArrayOutputStream(plain.length / 2 + 64);
            DataOutputStream header = new DataOutputStream(result);
            header.writeInt(MAGIC);
            header.writeInt(VERSION);
            header.write(compressedBytes.toByteArray());
            header.writeInt((int) crc.getValue());
            header.flush();
            byte[] encoded = result.toByteArray();
            if (encoded.length > MAX_BYTES) throw new ArchiveException("cave snapshot exceeds limit");
            return encoded;
        } catch (IOException exception) {
            throw new ArchiveException("cannot encode cave snapshot", exception);
        }
    }

    public static CaveSnapshot decode(byte[] encoded) throws ArchiveException {
        if (encoded == null || encoded.length < 16 || encoded.length > MAX_BYTES) {
            throw new ArchiveException("invalid cave snapshot length");
        }
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(encoded));
            if (input.readInt() != MAGIC) throw new ArchiveException("invalid cave snapshot magic");
            if (input.readInt() != VERSION) throw new ArchiveException("unsupported cave snapshot version");
            byte[] compressed = input.readNBytes(encoded.length - 8 - 4);
            int expectedCrc;
            try (DataInputStream crcInput = new DataInputStream(new ByteArrayInputStream(encoded, encoded.length - 4, 4))) {
                expectedCrc = crcInput.readInt();
            }
            ByteArrayOutputStream plainBytes = new ByteArrayOutputStream();
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = gzip.read(buffer)) >= 0) {
                    if (read > 0) plainBytes.write(buffer, 0, read);
                    if (plainBytes.size() > MAX_BYTES) throw new ArchiveException("cave snapshot body exceeds limit");
                }
            }
            byte[] plain = plainBytes.toByteArray();
            CRC32 crc = new CRC32();
            crc.update(plain);
            if ((int) crc.getValue() != expectedCrc) throw new ArchiveException("cave snapshot CRC mismatch");
            DataInputStream body = new DataInputStream(new ByteArrayInputStream(plain));
            ChunkPos chunk = new ChunkPos(body.readInt(), body.readInt());
            long revision = body.readLong();
            boolean scanned = body.readBoolean();
            boolean fullHeight = body.readBoolean();
            List<List<CaveColumnRun>> columns = new ArrayList<>(256);
            for (int columnIndex = 0; columnIndex < 256; columnIndex++) {
                int runCount = body.readInt();
                if (runCount < 0 || runCount > MAX_RUNS_PER_COLUMN) throw new ArchiveException("invalid cave run count");
                List<CaveColumnRun> runs = new ArrayList<>(runCount);
                for (int runIndex = 0; runIndex < runCount; runIndex++) {
                    int topY = body.readInt();
                    int floorY = body.readInt();
                    int argb = body.readInt();
                    int light = body.readUnsignedByte();
                    int flags = body.readUnsignedByte();
                    if ((flags & ~3) != 0) throw new ArchiveException("invalid cave run flags");
                    runs.add(new CaveColumnRun(topY, floorY, argb, light, (flags & 1) != 0, (flags & 2) != 0));
                }
                columns.add(List.copyOf(runs));
            }
            if (body.available() != 0) throw new ArchiveException("trailing cave snapshot body");
            return new CaveSnapshot(chunk, revision, scanned, fullHeight, columns);
        } catch (EOFException exception) {
            throw new ArchiveException("truncated cave snapshot", exception);
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ArchiveException archiveException) throw archiveException;
            throw new ArchiveException("cannot decode cave snapshot", exception);
        }
    }
}
