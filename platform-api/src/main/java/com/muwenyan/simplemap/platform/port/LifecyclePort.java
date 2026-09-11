package com.muwenyan.simplemap.platform.port;

public interface LifecyclePort {
    void register(Runnable callback);
}
