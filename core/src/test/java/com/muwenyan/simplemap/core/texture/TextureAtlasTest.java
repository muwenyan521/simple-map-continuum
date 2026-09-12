package com.muwenyan.simplemap.core.texture;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TextureAtlasTest {
    @Test
    void assemblesRgbaPixelsAtPackedRegions() {
        TextureTile tile = new TextureTile("a", 1, 1, new byte[]{1, 2, 3, 4});
        TextureAtlasLayout layout = TextureAtlasPacker.pack(List.of(tile), 4);
        TextureAtlas atlas = TextureAtlas.assemble(layout, List.of(tile));
        assertEquals(1, atlas.pixels()[0]);
        assertEquals(4, atlas.pixels()[3]);
    }
}
