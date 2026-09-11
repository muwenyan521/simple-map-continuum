package com.muwenyan.simplemap.core.session;

public final class LifecycleState {
    private RuntimeLifecycle state = RuntimeLifecycle.NEW;

    public synchronized RuntimeLifecycle state() {
        return state;
    }

    public synchronized void initialize() {
        require(RuntimeLifecycle.NEW);
        state = RuntimeLifecycle.READY;
    }

    public synchronized void attachWorld() {
        if (state != RuntimeLifecycle.READY && state != RuntimeLifecycle.WORLD_ATTACHED) {
            throw new IllegalStateException("runtime is not ready");
        }
        state = RuntimeLifecycle.WORLD_ATTACHED;
    }

    public synchronized void detachWorld() {
        require(RuntimeLifecycle.WORLD_ATTACHED);
        state = RuntimeLifecycle.READY;
    }

    public synchronized void stop() {
        if (state == RuntimeLifecycle.STOPPED) return;
        state = RuntimeLifecycle.STOPPED;
    }

    private void require(RuntimeLifecycle expected) {
        if (state != expected) throw new IllegalStateException("expected " + expected + ", got " + state);
    }
}
