package com.github.jarada.waygates.data;

public enum GateActivationParticles {

    HEAVY(10L),
    NORMAL(20L),
    LIGHT(30L),
    PULSE(60L),
    NONE(0L);

    private final Long size;

    GateActivationParticles(Long size) {
        this.size = size;
    }

    public Long getSize() {
        return size;
    }
}
