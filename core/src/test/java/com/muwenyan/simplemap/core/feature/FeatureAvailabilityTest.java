package com.muwenyan.simplemap.core.feature;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FeatureAvailabilityTest {
    @Test
    void unsupportedFeatureCannotBeEnabled() {
        MapFeatureFlags enabled = MapFeatureFlags.defaults();
        MapFeatureFlags supported = new MapFeatureFlags(true, true, true, true, true, false,
                true, true, true, true, false);
        assertThrows(IllegalArgumentException.class, () -> new FeatureAvailability(enabled, supported));
    }
}
