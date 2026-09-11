package com.muwenyan.simplemap.core.persistence;

import com.muwenyan.simplemap.core.surface.PackedSurfaceCell;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public record SurfaceRegionArchive(long[] pixels, int[] tints, String[] biomes, String[] blocks,
                                   long[] completeChunks, int version) {
    public static final int SIZE = 512;
    public static final int PIXELS = SIZE * SIZE;
    public static final int COVERAGE_WORDS = 16;
    private static final int MAGIC = 0x534D4150;
    private static final int MAX_PALETTE = 65_535;
    private static final int MAX_COMPRESSED_BYTES = 8 * 1024 * 1024;

    public SurfaceRegionArchive {
        if (pixels == null || pixels.length != PIXELS || tints == null || tints.length != PIXELS
                || biomes == null || blocks == null || completeChunks == null
                || completeChunks.length != COVERAGE_WORDS || version < 1 || version > 6) {
            throw new IllegalArgumentException("invalid surface archive");
        }
        biomes = biomes.clone();
        blocks = blocks.clone();
        pixels = pixels.clone();
        tints = tints.clone();
        completeChunks = completeChunks.clone();
    }

    @Override public long[] pixels() { return pixels.clone(); }
    @Override public int[] tints() { return tints.clone(); }
    @Override public long[] completeChunks() { return completeChunks.clone(); }
    @Override public String[] biomes() { return biomes.clone(); }
    @Override public String[] blocks() { return blocks.clone(); }

    public byte[] encode() throws IOException {
        if (biomes.length > MAX_PALETTE || blocks.length > MAX_PALETTE) throw new IOException("palette too large");
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(result);
        out.writeInt(MAGIC); out.writeInt(version); writePalette(out, biomes); writePalette(out, blocks);
        ByteArrayOutputStream rawBytes = new ByteArrayOutputStream();
        DataOutputStream raw = new DataOutputStream(rawBytes);
        int bytesPerPixel = version >= 3 ? 12 : version >= 2 ? 8 : 6;
        for (int i = 0; i < PIXELS; i++) {
            long pixel = pixels[i];
            raw.writeShort(PackedSurfaceCell.topY(pixel));
            raw.writeShort(PackedSurfaceCell.blockId(pixel));
            raw.writeByte(PackedSurfaceCell.biomeId(pixel));
            raw.writeByte(PackedSurfaceCell.flags(pixel));
            if (version >= 2) raw.writeShort(PackedSurfaceCell.floorY(pixel));
            if (version >= 3) raw.writeInt(tints[i]);
        }
        if (version >= 4) for (long word : completeChunks) raw.writeLong(word);
        raw.flush();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) { gzip.write(rawBytes.toByteArray()); }
        out.flush();
        return result.toByteArray();
    }

    public static SurfaceRegionArchive decode(byte[] encoded) throws IOException {
        if (encoded == null || encoded.length < 12 || encoded.length > MAX_COMPRESSED_BYTES) throw new IOException("invalid archive");
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(encoded));
        if (in.readInt() != MAGIC) throw new IOException("unexpected surface magic");
        int version = in.readInt();
        if (version < 1 || version > 6) throw new IOException("unsupported surface version");
        String[] biomes = readPalette(in); String[] blocks = readPalette(in);
        int bytesPerPixel = version >= 3 ? 12 : version >= 2 ? 8 : 6;
        int expected = PIXELS * bytesPerPixel + (version >= 4 ? COVERAGE_WORDS * Long.BYTES : 0);
        byte[] raw = readGzip(in, expected);
        DataInputStream pixelsIn = new DataInputStream(new ByteArrayInputStream(raw));
        long[] pixels = new long[PIXELS]; int[] tints = new int[PIXELS]; Arrays.fill(tints, 0);
        for (int i = 0; i < PIXELS; i++) {
            short top = pixelsIn.readShort(); short block = pixelsIn.readShort(); byte biome = pixelsIn.readByte(); byte flags = pixelsIn.readByte();
            short floor = version >= 2 ? pixelsIn.readShort() : top;
            pixels[i] = PackedSurfaceCell.pack(top, block, biome, flags, floor);
            if (version >= 3) tints[i] = pixelsIn.readInt();
        }
        long[] coverage = new long[COVERAGE_WORDS];
        if (version >= 4) for (int i = 0; i < coverage.length; i++) coverage[i] = pixelsIn.readLong();
        else Arrays.fill(coverage, -1L);
        if (pixelsIn.available() != 0) throw new IOException("trailing surface payload");
        return new SurfaceRegionArchive(pixels, tints, biomes, blocks, coverage, version);
    }

    private static void writePalette(DataOutputStream out, String[] palette) throws IOException {
        out.writeInt(palette.length);
        for (String value : palette) {
            if (value == null) throw new IOException("null palette entry");
            byte[] text = value.getBytes(StandardCharsets.UTF_8);
            if (text.length > 65_535) throw new IOException("palette entry too long");
            out.writeShort(text.length); out.write(text);
        }
    }

    private static String[] readPalette(DataInputStream in) throws IOException {
        int count = in.readInt();
        if (count < 0 || count > MAX_PALETTE) throw new IOException("invalid palette count");
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            int length = in.readUnsignedShort(); byte[] text = in.readNBytes(length);
            if (text.length != length) throw new EOFException("truncated palette");
            result[i] = new String(text, StandardCharsets.UTF_8);
        }
        return result;
    }

    private static byte[] readGzip(DataInputStream in, int expected) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(expected);
        try (GZIPInputStream gzip = new GZIPInputStream(in)) {
            byte[] buffer = new byte[8192]; int read;
            while ((read = gzip.read(buffer)) >= 0) {
                if (read > 0) output.write(buffer, 0, read);
                if (output.size() > expected) throw new IOException("oversized surface payload");
            }
        }
        if (output.size() != expected) throw new IOException("unexpected surface payload length");
        return output.toByteArray();
    }
}
