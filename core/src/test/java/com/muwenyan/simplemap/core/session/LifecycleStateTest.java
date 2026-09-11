package com.muwenyan.simplemap.core.session;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LifecycleStateTest {
    @Test
    void enforcesWorldLifecycleTransitions() {
        LifecycleState state = new LifecycleState();
        assertThrows(IllegalStateException.class, state::attachWorld);
        state.initialize();
        state.attachWorld();
        state.detachWorld();
        state.stop();
        assertEquals(RuntimeLifecycle.STOPPED, state.state());
    }
}
