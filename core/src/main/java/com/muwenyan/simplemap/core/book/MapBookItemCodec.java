package com.muwenyan.simplemap.core.book;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class MapBookItemCodec {
    private static final int MAGIC = 0x534D4249;
    private static final int VERSION = 1;
    private static final int MAX_TITLE_BYTES = 512;

    private MapBookItemCodec() { }

    public static byte[] encode(MapBookItemState state) throws IOException {
        if (state == null) throw new IOException("item state is null");
        byte[] title = state.title().getBytes(StandardCharsets.UTF_8);
        if (title.length > MAX_TITLE_BYTES) throw new IOException("title exceeds limit");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeInt(MAGIC);
            output.writeByte(VERSION);
            output.writeByte(state.status().ordinal());
            output.writeBoolean(state.id().isPresent());
            state.id().ifPresent(id -> writeUuid(output, id));
            output.writeShort(title.length);
            output.write(title);
        }
        return bytes.toByteArray();
    }

    public static MapBookItemState decode(byte[] encoded) throws IOException {
        if (encoded == null || encoded.length < 8) throw new IOException("invalid item state");
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(encoded))) {
            if (input.readInt() != MAGIC || input.readUnsignedByte() != VERSION) {
                throw new IOException("unsupported item state");
            }
            int status = input.readUnsignedByte();
            if (status < 0 || status >= MapBookStatus.values().length) throw new IOException("invalid item status");
            UUID id = input.readBoolean() ? readUuid(input) : null;
            int titleLength = input.readUnsignedShort();
            if (titleLength < 1 || titleLength > MAX_TITLE_BYTES || titleLength > input.available()) {
                throw new IOException("invalid item title");
            }
            String title = new String(input.readNBytes(titleLength), StandardCharsets.UTF_8);
            if (input.available() != 0) throw new IOException("trailing item state");
            return new MapBookItemState(MapBookStatus.values()[status], id, title);
        } catch (RuntimeException exception) {
            throw new IOException("cannot decode item state", exception);
        }
    }

    private static void writeUuid(DataOutputStream output, UUID value) {
        try {
            output.writeLong(value.getMostSignificantBits());
            output.writeLong(value.getLeastSignificantBits());
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static UUID readUuid(DataInputStream input) throws IOException {
        return new UUID(input.readLong(), input.readLong());
    }
}
