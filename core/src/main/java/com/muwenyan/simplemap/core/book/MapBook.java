package com.muwenyan.simplemap.core.book;

import com.muwenyan.simplemap.core.model.RegionPos;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class MapBook {
    private final UUID id;
    private final UUID owner;
    private final Map<UUID, BookPermission> permissions = new HashMap<>();
    private final Map<RegionPos, MapBookRegion> regions = new HashMap<>();
    private long revision;
    private MapBookStatus status = MapBookStatus.EMPTY;
    private LearningSession learning;

    public MapBook(UUID id, UUID owner) {
        this.id = Objects.requireNonNull(id, "id");
        this.owner = Objects.requireNonNull(owner, "owner");
        permissions.put(owner, BookPermission.OWNER);
    }

    public MapBook(MapBookSnapshot snapshot) {
        this(Objects.requireNonNull(snapshot, "snapshot").id(), snapshot.owner());
        if (snapshot.status() == MapBookStatus.LEARNING) {
            throw new IllegalArgumentException("learning books cannot be restored");
        }
        for (MapBookRegion region : snapshot.regions()) {
            regions.put(region.position(), region);
        }
        revision = snapshot.revision();
        status = snapshot.status();
        permissions.clear();
        permissions.putAll(snapshot.permissions());
        permissions.putIfAbsent(owner, BookPermission.OWNER);
    }

    public synchronized UUID id() { return id; }
    public synchronized UUID owner() { return owner; }
    public synchronized MapBookStatus status() { return status; }

    public synchronized void grant(UUID actor, UUID subject, BookPermission permission) {
        requireOwner(actor);
        if (subject == null || permission == null || subject.equals(owner)) {
            throw new IllegalArgumentException("invalid permission grant");
        }
        permissions.put(subject, permission);
    }

    public synchronized void revoke(UUID actor, UUID subject) {
        requireOwner(actor);
        if (subject == null || subject.equals(owner)) {
            throw new IllegalArgumentException("cannot revoke owner");
        }
        permissions.remove(subject);
    }

    public synchronized void save(UUID actor, RegionPos position, byte[] payload) {
        requireWrite(actor);
        if (learning != null) {
            throw new IllegalStateException("learning session is active");
        }
        putRegion(position, payload);
    }

    public synchronized void update(UUID actor, RegionPos position, byte[] payload) {
        save(actor, position, payload);
    }

    public synchronized LearningSession beginLearning(UUID actor) {
        requireWrite(actor);
        if (learning != null) {
            throw new IllegalStateException("learning session is active");
        }
        status = MapBookStatus.LEARNING;
        learning = new LearningSession(actor);
        return learning;
    }

    public synchronized void merge(UUID actor, MapBook source) {
        requireWrite(actor);
        Objects.requireNonNull(source, "source").assertReadable(actor);
        if (source == this) {
            return;
        }
        for (MapBookRegion candidate : source.snapshot().regions()) {
            MapBookRegion existing = regions.get(candidate.position());
            if (existing == null || candidate.revision() > existing.revision()) {
                regions.put(candidate.position(), candidate);
            }
        }
        if (!regions.isEmpty()) {
            revision = Math.addExact(revision, 1);
            status = MapBookStatus.WRITTEN;
        }
    }

    public synchronized MapBook copy(UUID actor, UUID newId, UUID newOwner) {
        assertReadable(actor);
        MapBook copy = new MapBook(newId, newOwner);
        for (MapBookRegion region : regions.values()) {
            copy.regions.put(region.position(), region);
        }
        copy.revision = revision;
        copy.status = status == MapBookStatus.EMPTY ? MapBookStatus.EMPTY : MapBookStatus.WRITTEN;
        return copy;
    }

    public synchronized MapBookSnapshot snapshot() {
        List<MapBookRegion> copy = new ArrayList<>(regions.values());
        copy.sort(Comparator.comparing((MapBookRegion r) -> r.position().x())
                .thenComparing(r -> r.position().z()));
        return new MapBookSnapshot(id, owner, status, revision, copy, permissions);
    }

    private void putRegion(RegionPos position, byte[] payload) {
        if (position == null || payload == null || payload.length == 0) {
            throw new IllegalArgumentException("position/payload");
        }
        revision = Math.addExact(revision, 1);
        regions.put(position, new MapBookRegion(position, revision, payload));
        status = MapBookStatus.WRITTEN;
    }

    private synchronized void assertReadable(UUID actor) {
        if (actor == null || !hasPermission(actor, BookPermission.READ)) {
            throw new SecurityException("book read permission required");
        }
    }

    private void requireWrite(UUID actor) {
        if (actor == null || !hasPermission(actor, BookPermission.WRITE)) {
            throw new SecurityException("book write permission required");
        }
    }

    private void requireOwner(UUID actor) {
        if (actor == null || !actor.equals(owner)) {
            throw new SecurityException("book owner permission required");
        }
    }

    private boolean hasPermission(UUID actor, BookPermission required) {
        BookPermission actual = permissions.get(actor);
        return actual != null && (actual == BookPermission.OWNER
                || actual == required || (required == BookPermission.READ && actual == BookPermission.WRITE));
    }

    public final class LearningSession implements AutoCloseable {
        private final UUID actor;
        private final Map<RegionPos, byte[]> staged = new HashMap<>();
        private boolean closed;

        private LearningSession(UUID actor) {
            this.actor = actor;
        }

        public synchronized void accept(RegionPos position, byte[] payload) {
            if (closed) {
                throw new IllegalStateException("learning session is closed");
            }
            if (position == null || payload == null || payload.length == 0) {
                throw new IllegalArgumentException("position/payload");
            }
            staged.put(position, payload.clone());
        }

        public synchronized int stagedCount() {
            return staged.size();
        }

        public synchronized void commit() {
            if (closed) {
                throw new IllegalStateException("learning session is closed");
            }
            requireWrite(actor);
            for (Map.Entry<RegionPos, byte[]> entry : staged.entrySet()) {
                putRegion(entry.getKey(), entry.getValue());
            }
            closed = true;
            learning = null;
        }

        public synchronized void abort() {
            if (!closed) {
                closed = true;
                learning = null;
                status = regions.isEmpty() ? MapBookStatus.EMPTY : MapBookStatus.WRITTEN;
            }
        }

        @Override
        public void close() {
            abort();
        }
    }
}
