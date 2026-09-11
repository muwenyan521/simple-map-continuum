package com.muwenyan.simplemap.core.persistence;

import java.io.IOException;

public final class ArchiveException extends IOException {
    private static final long serialVersionUID = 1L;
    public ArchiveException(String message) { super(message); }
    public ArchiveException(String message, Throwable cause) { super(message, cause); }
}
