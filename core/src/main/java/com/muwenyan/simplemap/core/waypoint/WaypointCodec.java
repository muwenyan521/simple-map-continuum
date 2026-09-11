package com.muwenyan.simplemap.core.waypoint;

import com.muwenyan.simplemap.core.navigation.Waypoint;
import com.muwenyan.simplemap.core.model.BlockPos;
import com.muwenyan.simplemap.core.model.DimensionId;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class WaypointCodec {
    private static final int MAGIC = 0x534D5057;
    private static final int VERSION = 1;
    private static final int MAX_WAYPOINTS = 4096;
    private static final int MAX_TEXT_BYTES = 1024;

    private WaypointCodec() {
    }

    public static byte[] encode(List<Waypoint> waypoints) throws IOException {
        if (waypoints == null || waypoints.size() > MAX_WAYPOINTS) {
            throw new IOException("waypoint count exceeds limit");
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeInt(MAGIC);
        out.writeByte(VERSION);
        out.writeInt(waypoints.size());
        for (Waypoint waypoint : waypoints) {
            if (waypoint == null) {
                throw new IOException("null waypoint");
            }
            out.writeLong(waypoint.id().getMostSignificantBits());
            out.writeLong(waypoint.id().getLeastSignificantBits());
            writeText(out, waypoint.dimension().value());
            out.writeInt(waypoint.position().x());
            out.writeInt(waypoint.position().y());
            out.writeInt(waypoint.position().z());
            writeText(out, waypoint.name());
            out.writeBoolean(waypoint.visible());
        }
        out.flush();
        return bytes.toByteArray();
    }

    public static List<Waypoint> decode(byte[] encoded) throws IOException {
        if (encoded == null) {
            throw new IOException("waypoint data is null");
        }
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(encoded));
            if (in.readInt() != MAGIC || in.readUnsignedByte() != VERSION) {
                throw new IOException("unsupported waypoint format");
            }
            int count = in.readInt();
            if (count < 0 || count > MAX_WAYPOINTS) {
                throw new IOException("invalid waypoint count");
            }
            List<Waypoint> result = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                UUID id = new UUID(in.readLong(), in.readLong());
                DimensionId dimension = new DimensionId(readText(in));
                BlockPos position = new BlockPos(in.readInt(), in.readInt(), in.readInt());
                result.add(new Waypoint(id, dimension, position, readText(in), in.readBoolean()));
            }
            if (in.available() != 0) {
                throw new IOException("trailing waypoint data");
            }
            return List.copyOf(result);
        } catch (EOFException | RuntimeException exception) {
            if (exception instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException("cannot decode waypoint data", exception);
        }
    }

    private static void writeText(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_TEXT_BYTES) {
            throw new IOException("waypoint text exceeds limit");
        }
        out.writeShort(bytes.length);
        out.write(bytes);
    }

    private static String readText(DataInputStream in) throws IOException {
        int length = in.readUnsignedShort();
        if (length > MAX_TEXT_BYTES || length > in.available()) {
            throw new IOException("invalid waypoint text length");
        }
        return new String(in.readNBytes(length), StandardCharsets.UTF_8);
    }
}
