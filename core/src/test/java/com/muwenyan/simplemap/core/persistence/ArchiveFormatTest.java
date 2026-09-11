package com.muwenyan.simplemap.core.persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArchiveFormatTest {
    @Test
    void detectsKnownMagicAndRejectsUnknown() throws Exception {
        byte[] encoded = ArchiveCodec.encode(ArchiveFormat.SMAP, new byte[]{1});
        assertEquals(ArchiveFormat.SMAP, ArchiveFormat.detect(encoded));
        assertThrows(ArchiveException.class, () -> ArchiveFormat.detect(new byte[]{0, 0, 0, 0}));
    }
}
