package com.muwenyan.simplemap.core.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

public final class MapBookHelloCodec {
    private static final int MAX_NAMES = 64;
    private static final int MAX_NAME_BYTES = 64;

    private MapBookHelloCodec() {
    }

    public static byte[] encode(MapBookHello hello) throws ProtocolException {
        if (hello == null) {
            throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "hello is null");
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeInt(hello.protocolMajor());
            out.writeInt(hello.protocolMinor());
            out.writeInt(hello.maxArchiveBytes());
            writeNames(out, hello.archiveFormats());
            writeNames(out, hello.capabilities());
            out.flush();
            if (bytes.size() > FrameCodec.MAX_BODY_BYTES) {
                throw new ProtocolException(ProtocolErrorCode.LIMIT_EXCEEDED, "hello body exceeds limit");
            }
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "cannot encode hello", exception);
        }
    }

    public static MapBookHello decode(byte[] body) throws ProtocolException {
        if (body == null || body.length > FrameCodec.MAX_BODY_BYTES) {
            throw new ProtocolException(ProtocolErrorCode.LIMIT_EXCEEDED, "invalid hello body");
        }
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(body));
            int major = in.readInt();
            int minor = in.readInt();
            int maxArchiveBytes = in.readInt();
            Set<String> formats = readNames(in);
            Set<String> capabilities = readNames(in);
            if (in.available() != 0) {
                throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "trailing hello bytes");
            }
            return new MapBookHello(major, minor, maxArchiveBytes, formats, capabilities);
        } catch (EOFException exception) {
            throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "truncated hello", exception);
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ProtocolException protocolException) {
                throw protocolException;
            }
            throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "cannot decode hello", exception);
        }
    }

    private static void writeNames(DataOutputStream out, Set<String> names) throws IOException {
        if (names.size() > MAX_NAMES) {
            throw new ProtocolException(ProtocolErrorCode.LIMIT_EXCEEDED, "too many names");
        }
        out.writeByte(names.size());
        for (String name : names) {
            byte[] bytes = name.getBytes(StandardCharsets.UTF_8);
            if (bytes.length == 0 || bytes.length > MAX_NAME_BYTES) {
                throw new ProtocolException(ProtocolErrorCode.LIMIT_EXCEEDED, "name exceeds limit");
            }
            out.writeByte(bytes.length);
            out.write(bytes);
        }
    }

    private static Set<String> readNames(DataInputStream in) throws IOException {
        int count = in.readUnsignedByte();
        if (count > MAX_NAMES) {
            throw new ProtocolException(ProtocolErrorCode.LIMIT_EXCEEDED, "too many names");
        }
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (int i = 0; i < count; i++) {
            int length = in.readUnsignedByte();
            if (length == 0 || length > MAX_NAME_BYTES || length > in.available()) {
                throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "invalid name length");
            }
            String name = new String(in.readNBytes(length), StandardCharsets.UTF_8);
            if (!names.add(name)) {
                throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "duplicate hello name");
            }
        }
        return Set.copyOf(names);
    }
}
