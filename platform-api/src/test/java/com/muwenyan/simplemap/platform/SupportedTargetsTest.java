package com.muwenyan.simplemap.platform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SupportedTargetsTest {
    @Test
    void matrixContainsEachDeclaredLoaderFamily() {
        assertEquals(4, SupportedTargets.current().size());
        assertEquals(2, SupportedTargets.current().stream().filter(value -> value.javaVersion() == 17).count());
        assertEquals(2, SupportedTargets.current().stream().filter(value -> value.javaVersion() == 21).count());
    }
}
