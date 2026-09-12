package com.muwenyan.simplemap.core.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Arrays;
import java.util.zip.CRC32;

public final class Smr2Container {
    private static final int MAGIC = 0x534D5232;
    private static final int VERSION = 2;
    private static final int RECORD_MAGIC = 0x52454332;
    private static final int HEADER_BYTES = 28;
    private static final int RECORD_HEADER_BYTES = 33;

    private Smr2Container() { }

    public static void append(Path path, Smr2Header header, Smr2Record record) throws IOException {
        Path target = Objects.requireNonNull(path, "path");
        Objects.requireNonNull(header, "header");
        Objects.requireNonNull(record, "record");
        Files.createDirectories(target.toAbsolutePath().normalize().getParent());
        if (!Files.exists(target)) Files.write(target, encodeHeader(header));
        Smr2ScanResult result = scan(target, header);
        if (result.damagedTail()) {
            try (var channel = java.nio.channels.FileChannel.open(target, StandardOpenOption.WRITE)) {
                channel.truncate(result.validBytes());
                channel.force(true);
            }
        }
        Files.write(target, encodeRecord(record), StandardOpenOption.APPEND);
    }

    public static Smr2ScanResult scan(Path path, Smr2Header expected) throws IOException {
        byte[] encoded = Files.readAllBytes(Objects.requireNonNull(path, "path"));
        if (encoded.length < HEADER_BYTES) throw new ArchiveException("truncated SMR2 header");
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(encoded))) {
            Smr2Header actual = readHeader(input);
            if (!actual.equals(Objects.requireNonNull(expected, "expected"))) {
                throw new ArchiveException("SMR2 header identity mismatch");
            }
            Map<Smr2Record.RecordKey, Smr2Record> latest = new LinkedHashMap<>();
            long validBytes = HEADER_BYTES;
            boolean damaged = false;
            while (input.available() > 0) {
                if (input.available() < RECORD_HEADER_BYTES) {
                    damaged = true;
                    break;
                }
                int before = input.available();
                try {
                    Smr2Record record = readRecord(input);
                    latest.merge(record.key(), record, Smr2Container::newer);
                    validBytes += before - input.available();
                } catch (ArchiveException | EOFException exception) {
                    damaged = true;
                    break;
                }
            }
            return new Smr2ScanResult(latest, validBytes, damaged);
        }
    }

    public static void compact(Path path, Smr2Header header) throws IOException {
        Path target = Objects.requireNonNull(path, "path");
        Smr2ScanResult result = scan(target, header);
        Path temporary = Files.createTempFile(target.toAbsolutePath().normalize().getParent(),
                target.getFileName().toString(), ".compact.tmp");
        boolean moved = false;
        try {
            Files.write(temporary, encodeHeader(header));
            for (Smr2Record record : result.latest().values()) {
                Files.write(temporary, encodeRecord(record), StandardOpenOption.APPEND);
            }
            Smr2ScanResult verified = scan(temporary, header);
            if (verified.damagedTail() || !sameRecords(verified.latest(), result.latest())) {
                throw new ArchiveException("SMR2 compaction verification failed");
            }
            if (Files.exists(target)) {
                Files.copy(target, target.resolveSibling(target.getFileName() + ".bak"),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;
        } finally {
            if (!moved) Files.deleteIfExists(temporary);
        }
    }

    private static byte[] encodeHeader(Smr2Header header) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(HEADER_BYTES);
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeInt(MAGIC);
            output.writeInt(VERSION);
            output.writeLong(header.worldIdentity());
            output.writeInt(header.regionX());
            output.writeInt(header.regionZ());
            output.writeInt(header.dataVersion());
        }
        return bytes.toByteArray();
    }

    private static Smr2Header readHeader(DataInputStream input) throws IOException {
        if (input.readInt() != MAGIC) throw new ArchiveException("unexpected SMR2 magic");
        if (input.readInt() != VERSION) throw new ArchiveException("unsupported SMR2 version");
        return new Smr2Header(input.readLong(), input.readInt(), input.readInt(), input.readInt());
    }

    private static byte[] encodeRecord(Smr2Record record) throws IOException {
        byte[] payload = record.payload();
        CRC32 crc = new CRC32();
        crc.update(payload);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(RECORD_HEADER_BYTES + payload.length);
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeInt(RECORD_MAGIC);
            output.writeByte(record.type().code());
            output.writeInt(record.localKey());
            output.writeLong(record.sourceRevision());
            output.writeLong(record.styleRevision());
            output.writeInt(payload.length);
            output.writeInt((int) crc.getValue());
            output.write(payload);
        }
        return bytes.toByteArray();
    }

    private static Smr2Record readRecord(DataInputStream input) throws IOException {
        if (input.readInt() != RECORD_MAGIC) throw new ArchiveException("invalid SMR2 record magic");
        Smr2RecordType type = Smr2RecordType.fromCode(input.readUnsignedByte());
        int localKey = input.readInt();
        long sourceRevision = input.readLong();
        long styleRevision = input.readLong();
        int length = input.readInt();
        int expectedCrc = input.readInt();
        if (length < 0 || length > ArchiveCodec.MAX_PAYLOAD_BYTES || length > input.available()) {
            throw new ArchiveException("invalid SMR2 record length");
        }
        byte[] payload = input.readNBytes(length);
        CRC32 crc = new CRC32();
        crc.update(payload);
        if ((int) crc.getValue() != expectedCrc) throw new ArchiveException("SMR2 record CRC mismatch");
        return new Smr2Record(type, localKey, sourceRevision, styleRevision, payload);
    }

    private static Smr2Record newer(Smr2Record left, Smr2Record right) {
        if (right.sourceRevision() != left.sourceRevision()) {
            return right.sourceRevision() > left.sourceRevision() ? right : left;
        }
        return right.styleRevision() >= left.styleRevision() ? right : left;
    }

    private static boolean sameRecords(Map<Smr2Record.RecordKey, Smr2Record> left,
                                       Map<Smr2Record.RecordKey, Smr2Record> right) {
        if (!left.keySet().equals(right.keySet())) return false;
        for (Smr2Record.RecordKey key : left.keySet()) {
            Smr2Record a = left.get(key);
            Smr2Record b = right.get(key);
            if (a.sourceRevision() != b.sourceRevision() || a.styleRevision() != b.styleRevision()
                    || !Arrays.equals(a.payload(), b.payload())) return false;
        }
        return true;
    }
}
