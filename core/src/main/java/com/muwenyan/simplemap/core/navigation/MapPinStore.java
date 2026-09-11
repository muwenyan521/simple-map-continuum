package com.muwenyan.simplemap.core.navigation;

import com.muwenyan.simplemap.core.model.DimensionId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class MapPinStore {
    private final List<MapPin> pins = new ArrayList<>();

    public synchronized void upsert(MapPin pin) {
        if (pin == null) throw new NullPointerException("pin");
        pins.removeIf(value -> value.id().equals(pin.id()));
        pins.add(pin);
    }

    public synchronized boolean remove(UUID id) {
        if (id == null) throw new NullPointerException("id");
        return pins.removeIf(value -> value.id().equals(id));
    }

    public synchronized List<MapPin> visible(DimensionId dimension) {
        if (dimension == null) throw new NullPointerException("dimension");
        return pins.stream().filter(value -> value.dimension().equals(dimension))
                .sorted(Comparator.comparing(MapPin::label).thenComparing(MapPin::id)).toList();
    }

    public synchronized List<MapPin> all() {
        return List.copyOf(pins);
    }
}
