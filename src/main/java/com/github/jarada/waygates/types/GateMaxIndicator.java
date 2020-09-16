package com.github.jarada.waygates.types;

public class GateMaxIndicator {

    private int amountCreated;
    private int amountAllowed;

    public GateMaxIndicator(int amountCreated, int amountAllowed) {
        this.amountCreated = amountCreated;
        this.amountAllowed = amountAllowed;
    }

    public int getAmountCreated() {
        return amountCreated;
    }

    public int getAmountAllowed() {
        return amountAllowed;
    }

    public boolean canCreate() {
        return amountAllowed == 0 || amountCreated < amountAllowed;
    }
}
