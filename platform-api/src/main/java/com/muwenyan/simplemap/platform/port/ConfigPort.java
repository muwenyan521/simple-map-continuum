package com.muwenyan.simplemap.platform.port;

import java.util.Optional;

public interface ConfigPort {
    Optional<String> read();
    void write(String encoded);
}
