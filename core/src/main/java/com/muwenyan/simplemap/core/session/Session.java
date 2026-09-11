package com.muwenyan.simplemap.core.session;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Session {
    private final long generation;
    private final AtomicBoolean cancelled = new AtomicBoolean();
    public Session(long generation) { if (generation < 0) throw new IllegalArgumentException("generation"); this.generation = generation; }
    public long generation() { return generation; }
    public CancellationToken token() { return cancelled::get; }
    public void cancel() { cancelled.set(true); }
    public boolean accepts(long taskGeneration) { return !cancelled.get() && taskGeneration == generation; }
}
