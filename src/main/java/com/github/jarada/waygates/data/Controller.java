package com.github.jarada.waygates.data;

import com.github.jarada.waygates.menus.Menu;
import com.google.gson.Gson;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Controller {

    private static Gson gson;

    private UUID uuid;
    private UUID owner;

    private BlockLocation location;

    private transient Gate gate;
    private String gateUuid;

    private transient Set<Menu> activeMenus;

    public Controller(UUID owner, BlockLocation location) {
        this.owner = owner;
        this.location = location;
    }

    /* Getters / Setters */

    public UUID getUUID() {
        if (uuid == null)
            uuid = UUID.randomUUID();

        return uuid;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public BlockLocation getLocation() {
        return location;
    }

    public Gate getGate() {
        return gate;
    }

    public void setGate(Gate gate) {
        this.gate = gate;
    }

    public String getGateUuid() {
        if (gateUuid == null && getGate() != null)
            return getGate().getUUID().toString();
        return gateUuid;
    }

    @SuppressWarnings("unused")
    public void setGateUuid(String gateUuid) {
        this.gateUuid = gateUuid;
    }

    public String getWorldName() {
        return location.getWorldName();
    }

    @SuppressWarnings("unused")
    public World getWorld() {
        return location.getWorld();
    }

    /* Menus */

    public void addActiveMenu(Menu menu) {
        if (activeMenus == null)
            activeMenus = new HashSet<>();
        activeMenus.add(menu);
    }

    public void removeActiveMenu(Menu menu) {
        if (activeMenus != null)
            activeMenus.remove(menu);
    }

    public void closeActiveMenus() {
        if (activeMenus != null) {
            for (Menu activeMenu : activeMenus)
                activeMenu.close();
        }
    }

    /* Serialization */

    private static Gson getGson() {
        if (gson == null)
            gson = new Gson();
        return gson;
    }

    public static Controller fromJson(String json) {
        return getGson().fromJson(json, Controller.class);
    }

    public String toJson() {
        if (gate != null)
            gateUuid = gate.getUUID().toString();
        return getGson().toJson(this);
    }
    
}
