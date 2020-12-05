package com.github.jarada.waygates.listeners;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.callbacks.IconCallback;
import com.github.jarada.waygates.data.Gate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

public class IconListener {

    private PluginMain pm;
    private IconCallback iconCallback;
    private Gate listeningGate;
    private final BukkitTask timeout;

    public IconListener(IconCallback iconCallback) {
        this.pm = PluginMain.getPluginInstance();
        this.iconCallback = iconCallback;
        this.listeningGate = iconCallback.getCurrentWaygate();
        this.listeningGate.addIconListener(this);

        // 20L is 1 second, we give 30s
        timeout = Bukkit.getScheduler().runTaskLater(pm, this::destroy, 600L);
    }

    public boolean isForPlayer(Player player) {
        return player.equals(iconCallback.getPlayer());
    }

    public void notify(Player player, Material icon) {
        // Verify Player
        if (player.equals(iconCallback.getPlayer()) && !timeout.isCancelled()) {
            timeout.cancel();

            // Verify Input
            if (iconCallback.verify(icon)) {
                // Success
                iconCallback.success(icon);
            } else {
                // Failure
                iconCallback.failure();
            }

            // Destroy
            listeningGate.removeIconListener(this);
            Bukkit.getScheduler().scheduleSyncDelayedTask(pm, this::destroy, 20L);
        }
    }

    public void expire() {
        timeout.cancel();
        clear();
    }

    private void destroy() {
        iconCallback.callback();
        listeningGate.removeIconListener(this);
        clear();
    }

    private void clear() {
        this.pm = null;
        this.iconCallback = null;
        this.listeningGate = null;
    }

}
