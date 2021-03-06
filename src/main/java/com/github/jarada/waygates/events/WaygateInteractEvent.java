package com.github.jarada.waygates.events;

import com.github.jarada.waygates.data.Gate;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class WaygateInteractEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean                  cancelled;

    private final Player p;
    private final Gate wg;
    private final Block bl;
    private final Action a;
    private final ItemStack item;

    public WaygateInteractEvent(Player p, Gate wg, Block bl, Action a, ItemStack item) {
        this.p = p;
        this.wg = wg;
        this.bl = bl;
        this.a = a;
        this.item = item;
    }

    public Player getPlayer() {
        return p;
    }

    public Gate getWaygate() {
        return wg;
    }

    public Block getClickedBlock() {
        return bl;
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

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
