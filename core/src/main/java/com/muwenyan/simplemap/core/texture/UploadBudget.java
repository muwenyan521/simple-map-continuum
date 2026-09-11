package com.muwenyan.simplemap.core.texture;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public final class UploadBudget {
    private final Queue<UploadRequest> pending = new ArrayDeque<>();

    public synchronized void enqueue(UploadRequest request) {
        pending.add(Objects.requireNonNull(request, "request"));
    }

    public synchronized List<UploadRequest> drain(long budgetBytes) {
        if (budgetBytes < 0) {
            throw new IllegalArgumentException("budgetBytes");
        }
        List<UploadRequest> result = new ArrayList<>();
        long used = 0;
        while (!pending.isEmpty()) {
            UploadRequest next = pending.peek();
            if (next.bytes() > budgetBytes - used) {
                break;
            }
            used += next.bytes();
            result.add(pending.remove());
        }
        return List.copyOf(result);
    }

    public synchronized int pendingCount() {
        return pending.size();
    }

    public record UploadRequest(String id, long bytes, long revision) {
        public UploadRequest {
            if (id == null || id.isBlank() || bytes < 1 || revision < 0) {
                throw new IllegalArgumentException("invalid upload request");
            }
        }
    }
}
