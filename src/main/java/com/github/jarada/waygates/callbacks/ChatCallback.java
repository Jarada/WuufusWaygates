package com.github.jarada.waygates.callbacks;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.BlockLocation;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.util.Util;
import org.bukkit.entity.Player;

public abstract class ChatCallback {

    private final Player player;
    private final Gate currentWaygate;
    private final WaygateManager waygateManager;

    // TODO Create Item Listener/Callback for Updating Network Icons
    public ChatCallback(Player player, Gate currentWaygate) {
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

    public abstract boolean verify(String chat);

    public abstract void success(String chat);

    public abstract void failure();

    public abstract void callback();

}
