package com.github.jarada.waygates.listeners;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.callbacks.ChatCallback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;

public class ChatListener implements Listener {

    private PluginMain pm;
    private ChatCallback chatCallback;
    private BukkitTask timeout;

    public ChatListener(ChatCallback chatCallback) {
        this.pm = PluginMain.getPluginInstance();
        this.chatCallback = chatCallback;
        Bukkit.getPluginManager().registerEvents(this, pm);

        timeout = Bukkit.getScheduler().runTaskLater(pm, new Runnable() {
            @Override
            public void run() {
                destroy();
            }
        }, 600L); // 20L is 1 second, we give 30s
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
            Bukkit.getScheduler().scheduleSyncDelayedTask(pm, new Runnable() {
                @Override
                public void run() {
                    destroy();
                }
            }, 20L);
        }
    }

    private void destroy() {
        chatCallback.callback();
        this.pm = null;
        this.chatCallback = null;
        HandlerList.unregisterAll(this);
    }

}
