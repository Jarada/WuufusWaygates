package com.github.jarada.waygates.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;

public class WaygateKeyUseEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean                  cancelled;

    private Player p;
    private Action a;
    private Block  b;

    public WaygateKeyUseEvent(Player p, Action a, Block b) {
        this.p = p;
        this.a = a;
        this.b = b;
    }

    public Player getPlayer() {
        return p;
    }
    
    public Action getAction() {
        return a;
    }

    public Block getClickedBlock() {
        return b;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
