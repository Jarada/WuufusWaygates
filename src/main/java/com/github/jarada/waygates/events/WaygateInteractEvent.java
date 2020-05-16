package com.github.jarada.waygates.events;

import com.github.jarada.waygates.data.Gate;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public class WaygateInteractEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean                  cancelled;

    private Player p;
    private Gate wg;
    private Action a;
    private ItemStack item;

    public WaygateInteractEvent(Player p, Gate wg, Action a, ItemStack item) {
        this.p = p;
        this.wg = wg;
        this.a = a;
        this.item = item;
    }

    public Player getPlayer() {
        return p;
    }

    public Gate getWaygate() {
        return wg;
    }

    public Action getAction() {
        return a;
    }

    public ItemStack getItem() {
        return item;
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
