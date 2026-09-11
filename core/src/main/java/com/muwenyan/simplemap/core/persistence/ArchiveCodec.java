package com.muwenyan.simplemap.core.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;

public final class ArchiveCodec {
    public static final int MAX_PAYLOAD_BYTES = 4 * 1024 * 1024;
    private static final int HEADER_BYTES = 4 + 1 + 4;
    private ArchiveCodec() { }

    public static byte[] encode(ArchiveFormat format, byte[] payload) throws ArchiveException {
        if (payload == null || payload.length > MAX_PAYLOAD_BYTES) throw new ArchiveException("payload exceeds limit");
        byte[] magic = format.magic().getBytes(StandardCharsets.US_ASCII);
        CRC32 crc = new CRC32();
        crc.update(payload);
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(HEADER_BYTES + payload.length + 4);
            DataOutputStream out = new DataOutputStream(bytes);
            out.write(magic); out.writeByte(format.version()); out.writeInt(payload.length); out.write(payload); out.writeInt((int) crc.getValue()); out.flush();
            return bytes.toByteArray();
        } catch (IOException e) { throw new ArchiveException("cannot encode archive", e); }
    }

    public static byte[] decode(ArchiveFormat expected, byte[] encoded) throws ArchiveException {
        if (encoded == null || encoded.length < HEADER_BYTES + 4) throw new ArchiveException("truncated archive");
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(encoded));
            byte[] magic = in.readNBytes(4);
            if (!Arrays.equals(magic, expected.magic().getBytes(StandardCharsets.US_ASCII))) throw new ArchiveException("unexpected archive magic");
            int version = in.readUnsignedByte();
            if (!expected.supports(version)) throw new ArchiveException("unsupported archive version: " + version);
            int length = in.readInt();
            if (length < 0 || length > MAX_PAYLOAD_BYTES || in.available() != length + 4) throw new ArchiveException("invalid archive length");
            byte[] payload = in.readNBytes(length);
            int expectedCrc = in.readInt();
            CRC32 crc = new CRC32(); crc.update(payload);
            if ((int) crc.getValue() != expectedCrc) throw new ArchiveException("archive CRC mismatch");
            return payload;
        } catch (IOException e) { if (e instanceof ArchiveException a) throw a; throw new ArchiveException("cannot decode archive", e); }
    }
}
