package com.github.jarada.waygates.types;

public enum MenuSize {

    COMPACT(18),
    SMALL(27),
    MEDIUM(36),
    LARGE(45),
    MAX(54),
    RESIZE(0);

    public static final int STEP_SIZE = 9;

    private int menuSize;

    MenuSize(int menuSize) {
        this.menuSize = menuSize;
    }

    public int getMenuSize() {
        return menuSize;
    }

    public int getTopAreaSize() {
        return (menuSize == 0) ? menuSize : menuSize - STEP_SIZE;
    }

    public static int getAppropriateMenuSize(int count) {
        if (count <= MenuSize.COMPACT.getTopAreaSize())
            return MenuSize.COMPACT.getMenuSize();
        if (count <= MenuSize.SMALL.getTopAreaSize())
            return MenuSize.SMALL.getMenuSize();
        if (count <= MenuSize.MEDIUM.getTopAreaSize())
            return MenuSize.MEDIUM.getMenuSize();
        if (count <= MenuSize.LARGE.getTopAreaSize())
            return MenuSize.LARGE.getMenuSize();
        if (count <= MenuSize.MAX.getTopAreaSize())
            return MenuSize.MAX.getMenuSize();
        return MenuSize.COMPACT.getMenuSize();
    }
}
