package com.muwenyan.simplemap.platform.port;

import com.muwenyan.simplemap.core.render.MapRenderFrame;

public interface RenderPort {
    void publish(MapRenderFrame frame);
}
