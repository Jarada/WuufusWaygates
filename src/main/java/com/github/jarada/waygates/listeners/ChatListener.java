package com.github.jarada.waygates.listeners;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.callbacks.ChatCallback;
import com.github.jarada.waygates.menus.MenuExpirable;
import com.github.jarada.waygates.menus.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class ChatListener implements Listener, MenuExpirable {

    private PluginMain pm;
    private ChatCallback chatCallback;
    private Player player;
    private final BukkitTask timeout;

    public ChatListener(@NotNull ChatCallback chatCallback) {
        this.pm = PluginMain.getPluginInstance();
        this.chatCallback = chatCallback;
        this.player = chatCallback.getPlayer();
        Bukkit.getPluginManager().registerEvents(this, pm);
        MenuManager.setExpirable(player, this);

        // 20L is 1 second, we give 30s
        timeout = Bukkit.getScheduler().runTaskLater(pm, this::finish, 600L);
    }

    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent e){
        // Verify Player
        if (e.getPlayer().equals(chatCallback.getPlayer()) && !timeout.isCancelled()) {
            timeout.cancel();
            e.setCancelled(true);

            // Verify Input
            if (chatCallback.verify(e.getMessage())) {
                // Success
                chatCallback.success(e.getMessage());
            } else {
                // Failure
                chatCallback.failure();
            }

            // Destroy
            Bukkit.getScheduler().scheduleSyncDelayedTask(pm, this::finish, 20L);
        }
    }

    public void finish() {
        if (chatCallback != null) {
            chatCallback.callback();
            destroy();
        }
    }

    @Override
    public void expire() {
        timeout.cancel();
        chatCallback.expire();
        destroy();
    }

    private void destroy() {
        MenuManager.clearExpirable(player);
        this.pm = null;
        this.chatCallback = null;
        this.player = null;
        HandlerList.unregisterAll(this);
    }

}
