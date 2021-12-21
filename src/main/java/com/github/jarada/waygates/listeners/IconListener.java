package com.github.jarada.waygates.listeners;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.callbacks.IconCallback;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.menus.MenuExpirable;
import com.github.jarada.waygates.menus.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class IconListener implements MenuExpirable {

    private PluginMain pm;
    private IconCallback iconCallback;
    private Player player;
    private Gate listeningGate;
    private final BukkitTask timeout;

    public IconListener(@NotNull IconCallback iconCallback) {
        this.pm = PluginMain.getPluginInstance();
        this.iconCallback = iconCallback;
        this.player = iconCallback.getPlayer();
        this.listeningGate = iconCallback.getCurrentWaygate();
        this.listeningGate.addIconListener(this);
        MenuManager.setExpirable(iconCallback.getPlayer(), this);

        // 20L is 1 second, we give 30s
        timeout = Bukkit.getScheduler().runTaskLater(pm, this::finish, 600L);
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
            Bukkit.getScheduler().scheduleSyncDelayedTask(pm, this::finish, 20L);
        }
    }

    @Override
    public void expire() {
        timeout.cancel();
        iconCallback.expire();
        destroy();
    }

    private void finish() {
        if (iconCallback != null) {
            iconCallback.callback();
            destroy();
        }
    }

    private void destroy() {
        MenuManager.clearExpirable(player);
        listeningGate.removeIconListener(this);
        this.pm = null;
        this.iconCallback = null;
        this.listeningGate = null;
        this.player = null;
    }

}
