package com.muwenyan.simplemap.platform;

import com.muwenyan.simplemap.platform.file.FileConfigPort;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FileConfigPortTest {
    @TempDir Path temporary;

    @Test
    void writesVerifiesAndBacksUpConfig() throws Exception {
        Path path = temporary.resolve("config/simplemap.cfg");
        FileConfigPort port = new FileConfigPort(path);
        port.write("one");
        port.write("two");
        org.junit.jupiter.api.Assertions.assertEquals("two", port.read().orElseThrow());
        org.junit.jupiter.api.Assertions.assertEquals("one", Files.readString(path.resolveSibling("simplemap.cfg.bak")));
        assertTrue(Files.isRegularFile(path));
    }
}
