package com.muwenyan.simplemap.core;

import com.muwenyan.simplemap.core.persistence.ArchiveCodec;
import com.muwenyan.simplemap.core.persistence.ArchiveFormat;
import com.muwenyan.simplemap.core.persistence.AtomicArchiveStore;
import com.muwenyan.simplemap.core.protocol.FrameCodec;
import com.muwenyan.simplemap.core.protocol.MapBookFrame;
import com.muwenyan.simplemap.core.protocol.MapBookHello;
import com.muwenyan.simplemap.core.protocol.MapBookMessageType;
import com.muwenyan.simplemap.core.protocol.MapBookNegotiator;
import com.muwenyan.simplemap.core.protocol.ProtocolErrorCode;
import com.muwenyan.simplemap.core.protocol.ProtocolException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

public final class ProtocolPersistenceSelfTest {
    private ProtocolPersistenceSelfTest() { }

    public static void main(String[] args) throws Exception {
        frameRoundTripAndMalformedInputs();
        negotiationUsesSharedLimitsAndCapabilities();
        crcAndAtomicStoreRejectCorruption();
        System.out.println("PROTOCOL_PERSISTENCE_SELF_TEST_PASS");
    }

    private static void frameRoundTripAndMalformedInputs() throws Exception {
        MapBookFrame source = new MapBookFrame(1, MapBookMessageType.REGION_DATA, UUID.randomUUID(), new byte[] {1, 2, 3});
        if (!source.equals(FrameCodec.decode(FrameCodec.encode(source)))) throw new AssertionError("frame round trip");
        byte[] invalidMagic = FrameCodec.encode(source); invalidMagic[0] ^= 1;
        expect(ProtocolErrorCode.INVALID_MAGIC, invalidMagic);
        byte[] invalidLength = FrameCodec.encode(source); invalidLength[21] = 0;
        invalidLength[22] = 0; invalidLength[23] = 0; invalidLength[24] = 9;
        expect(ProtocolErrorCode.INVALID_LENGTH, invalidLength);
    }

    private static void negotiationUsesSharedLimitsAndCapabilities() throws Exception {
        MapBookHello left = new MapBookHello(1, 2, 200, Set.of("SMR2"), Set.of("save", "learn"));
        MapBookHello right = new MapBookHello(1, 1, 100, Set.of("SMR2"), Set.of("save"));
        MapBookHello negotiated = MapBookNegotiator.negotiate(left, right);
        if (negotiated.maxArchiveBytes() != 100 || !negotiated.capabilities().equals(Set.of("save"))) throw new AssertionError("negotiation");
    }

    private static void crcAndAtomicStoreRejectCorruption() throws Exception {
        if (!ArchiveFormat.SMAP.supports(1) || !ArchiveFormat.SMAP.supports(6)
                || ArchiveFormat.SMAP.supports(7) || ArchiveFormat.SMAP.version() != 6) {
            throw new AssertionError("SMAP version range");
        }
        byte[] encoded = ArchiveCodec.encode(ArchiveFormat.SMR2, new byte[] {7, 8});
        encoded[encoded.length - 1] ^= 1;
        try { ArchiveCodec.decode(ArchiveFormat.SMR2, encoded); throw new AssertionError("CRC accepted"); }
        catch (com.muwenyan.simplemap.core.persistence.ArchiveException expected) { }
        Path directory = Files.createTempDirectory("simple-map-core-test");
        Path target = directory.resolve("region.smr2");
        AtomicArchiveStore.write(target, ArchiveFormat.SMR2, new byte[] {4, 5, 6});
        if (!java.util.Arrays.equals(new byte[] {4, 5, 6}, AtomicArchiveStore.read(target, ArchiveFormat.SMR2))) throw new AssertionError("atomic store");
        Files.deleteIfExists(target); Files.deleteIfExists(directory);
    }

    private static void expect(ProtocolErrorCode code, byte[] wire) throws Exception {
        try { FrameCodec.decode(wire); throw new AssertionError("malformed frame accepted"); }
        catch (ProtocolException exception) { if (exception.code() != code) throw exception; }
    }
}
