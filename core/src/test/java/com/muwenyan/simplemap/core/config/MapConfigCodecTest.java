package com.muwenyan.simplemap.core.config;

import com.muwenyan.simplemap.core.model.ColorMode;
import com.muwenyan.simplemap.core.model.MapMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MapConfigCodecTest {
    @Test
    void roundTripIsStable() {
        MapConfig config = new MapConfig(false, MapMode.CAVE, ColorMode.VANILLA, 24, 2 * 1024 * 1024);
        assertEquals(config, MapConfigCodec.decode(MapConfigCodec.encode(config)));
    }

    @Test
    void versionOneAliasesMigrate() {
        String legacy = "version=1\nenabled=true\nmode=surface\ncolorMode=accurate\n"
                + "distance=12\nuploadBytes=1048576\n";
        MapConfig config = MapConfigCodec.decode(legacy);
        assertEquals(12, config.renderDistance());
        assertEquals(1048576, config.maxUploadBytes());
    }

    @Test
    void malformedAndUnknownValuesFailClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> MapConfigCodec.decode("version=99\nenabled=true"));
        assertThrows(IllegalArgumentException.class,
                () -> MapConfigCodec.decode("version=2\nenabled=true\nmode=surface\n"
                        + "colorMode=accurate\nrenderDistance=12\nmaxUploadBytes=1048576\nextra=x\n"));
    }
}
