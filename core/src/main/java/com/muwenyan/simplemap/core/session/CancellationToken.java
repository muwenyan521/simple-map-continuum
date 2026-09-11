package com.muwenyan.simplemap.core.session;

public interface CancellationToken {
    boolean isCancelled();
    default void throwIfCancelled() { if (isCancelled()) throw new CancellationException(); }
    final class CancellationException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public CancellationException() { super("operation cancelled"); }
    }
}
