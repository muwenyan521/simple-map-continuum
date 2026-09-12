package com.muwenyan.simplemap.core.config;

import org.junit.jupiter.api.Test;
import com.muwenyan.simplemap.core.color.ColorProfile;
import com.muwenyan.simplemap.core.style.MapStyle;
import com.muwenyan.simplemap.core.style.ReliefMode;
import com.muwenyan.simplemap.core.style.WaterShading;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientMapConfigCodecTest {
    @Test
    void roundTripsComposedClientConfig() {
        ClientMapConfig defaults = ClientMapConfig.defaults();
        ClientMapConfig source = new ClientMapConfig(defaults.map(), defaults.minimap(), defaults.cave(),
                new MapStyle(ColorProfile.VIBRANT, ReliefMode.THREE_D, WaterShading.DEPTH, true,
                        Map.of(7, 0xff112233)), defaults.features());
        ClientMapConfig decoded = ClientMapConfigCodec.decode(ClientMapConfigCodec.encode(source));
        assertEquals(source.map(), decoded.map());
        assertEquals(source.minimap(), decoded.minimap());
        assertEquals(source.cave(), decoded.cave());
        assertEquals(source.features(), decoded.features());
        assertEquals(source.style(), decoded.style());
    }
}
