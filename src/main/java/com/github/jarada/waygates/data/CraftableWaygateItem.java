package com.github.jarada.waygates.data;

public enum CraftableWaygateItem {
    WAYGATE_CONSTRUCTOR("waygateconstructor"),
    WAYGATE_KEY("waygatekey"),
    WAYGATE_CONTROL("waygatecontrol");

    private final String key;

    CraftableWaygateItem(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

