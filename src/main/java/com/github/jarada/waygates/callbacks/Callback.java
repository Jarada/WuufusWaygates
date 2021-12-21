package com.github.jarada.waygates.callbacks;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.BlockLocation;
import com.github.jarada.waygates.data.Gate;
import org.bukkit.entity.Player;

public abstract class Callback<T> {

    private final Player player;
    private final Gate currentWaygate;
    private final WaygateManager waygateManager;
    
    public Callback(Player player, Gate currentWaygate) {
        this.player = player;
        this.currentWaygate = currentWaygate;
        this.waygateManager = WaygateManager.getManager();
    }

    public Player getPlayer() {
        return player;
    }

    public Gate getCurrentWaygate() {
        return currentWaygate;
    }

    public boolean isPlayerNearGate() {
        return waygateManager.getGatesNearLocation(new BlockLocation(getPlayer().getLocation()))
                .contains(getCurrentWaygate());
    }

    public abstract boolean verify(T obj);

    public abstract void success(T obj);

    public abstract void failure();

    public abstract void callback();

    public abstract void expire();

}
