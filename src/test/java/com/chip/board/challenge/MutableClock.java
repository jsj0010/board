package com.chip.board.challenge;

import java.time.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class MutableClock extends Clock {
    private final ZoneId zone;
    private final AtomicReference<Instant> instantRef;

    public MutableClock(Instant initialInstant, ZoneId zone) {
        this.instantRef = new AtomicReference<>(Objects.requireNonNull(initialInstant));
        this.zone = Objects.requireNonNull(zone);
    }

    public void setInstant(Instant newInstant) {
        this.instantRef.set(Objects.requireNonNull(newInstant));
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant(), zone);
    }

    @Override
    public Instant instant() {
        return instantRef.get();
    }
}
