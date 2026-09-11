package com.muwenyan.simplemap.core.color;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BlockColorResolverTest {
    @Test
    void overrideTakesPrecedenceOverVanillaColor() {
        BlockColorResolver resolver = new BlockColorResolver(Map.of("minecraft:stone", 0xff112233), ColorProfile.BALANCED);
        assertEquals(0xff112233, resolver.resolve("minecraft:stone", 0xffaabbcc));
        assertEquals(0xffaabbcc, resolver.resolve("minecraft:dirt", 0xffaabbcc));
    }
}
