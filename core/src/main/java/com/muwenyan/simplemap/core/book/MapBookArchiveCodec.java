package com.muwenyan.simplemap.core.book;

import com.muwenyan.simplemap.core.model.RegionPos;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MapBookArchiveCodec {
    private static final int MAGIC = 0x534D424B;
    private static final int VERSION = 1;
    private static final int MAX_REGIONS = 4096;
    private static final int MAX_PAYLOAD = 4 * 1024 * 1024;

    private MapBookArchiveCodec() {
    }

    public static byte[] encode(MapBook book) throws IOException {
        if (book == null) {
            throw new IOException("book is null");
        }
        MapBookSnapshot snapshot = book.snapshot();
        if (snapshot.status() == MapBookStatus.LEARNING || snapshot.regions().size() > MAX_REGIONS) {
            throw new IOException("book cannot be archived");
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeInt(MAGIC);
        out.writeByte(VERSION);
        writeUuid(out, snapshot.id());
        writeUuid(out, snapshot.owner());
        out.writeByte(snapshot.status().ordinal());
        out.writeLong(snapshot.revision());
        out.writeInt(snapshot.regions().size());
        for (MapBookRegion region : snapshot.regions()) {
            byte[] payload = region.payload();
            if (payload.length > MAX_PAYLOAD) {
                throw new IOException("region payload exceeds limit");
            }
            out.writeInt(region.position().x());
            out.writeInt(region.position().z());
            out.writeLong(region.revision());
            out.writeInt(payload.length);
            out.write(payload);
        }
        out.flush();
        if (bytes.size() > MAX_PAYLOAD) {
            throw new IOException("book archive exceeds limit");
        }
        return bytes.toByteArray();
    }

    public static MapBook decode(byte[] encoded) throws IOException {
        if (encoded == null || encoded.length > MAX_PAYLOAD) {
            throw new IOException("invalid book archive");
        }
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(encoded));
            if (in.readInt() != MAGIC || in.readUnsignedByte() != VERSION) {
                throw new IOException("unsupported book archive");
            }
            UUID id = readUuid(in);
            UUID owner = readUuid(in);
            int statusValue = in.readUnsignedByte();
            if (statusValue < 0 || statusValue >= MapBookStatus.values().length) {
                throw new IOException("invalid book status");
            }
            MapBookStatus status = MapBookStatus.values()[statusValue];
            long revision = in.readLong();
            int count = in.readInt();
            if (count < 0 || count > MAX_REGIONS) {
                throw new IOException("invalid region count");
            }
            List<MapBookRegion> regions = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                RegionPos position = new RegionPos(in.readInt(), in.readInt());
                long regionRevision = in.readLong();
                int length = in.readInt();
                if (length < 1 || length > MAX_PAYLOAD || length > in.available()) {
                    throw new IOException("invalid region payload");
                }
                regions.add(new MapBookRegion(position, regionRevision, in.readNBytes(length)));
            }
            if (in.available() != 0) {
                throw new IOException("trailing book archive data");
            }
            return new MapBook(new MapBookSnapshot(id, owner, status, revision, regions));
        } catch (EOFException | RuntimeException exception) {
            if (exception instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException("cannot decode book archive", exception);
        }
    }

    private static void writeUuid(DataOutputStream out, UUID value) throws IOException {
        out.writeLong(value.getMostSignificantBits());
        out.writeLong(value.getLeastSignificantBits());
    }

    private static UUID readUuid(DataInputStream in) throws IOException {
        return new UUID(in.readLong(), in.readLong());
    }
}
