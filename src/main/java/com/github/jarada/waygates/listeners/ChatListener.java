package com.github.jarada.waygates.listeners;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.callbacks.ChatCallback;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;

public class ChatListener implements Listener {

    private PluginMain pm;
    private ChatCallback chatCallback;
    private final BukkitTask timeout;

    public ChatListener(ChatCallback chatCallback) {
        this.pm = PluginMain.getPluginInstance();
        this.chatCallback = chatCallback;
        Bukkit.getPluginManager().registerEvents(this, pm);

        // 20L is 1 second, we give 30s
        timeout = Bukkit.getScheduler().runTaskLater(pm, this::destroy, 600L);
    }

    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent e){
        // Verify Player
        if (e.getPlayer().equals(chatCallback.getPlayer())) {
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
            Bukkit.getScheduler().scheduleSyncDelayedTask(pm, this::destroy, 20L);
        }
    }

    private void destroy() {
        chatCallback.callback();
        this.pm = null;
        this.chatCallback = null;
        HandlerList.unregisterAll(this);
    }

}
