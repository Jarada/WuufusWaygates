package com.github.jarada.waygates.listeners;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.BlockLocation;
import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.events.WaygateKeyUseEvent;
import com.github.jarada.waygates.menus.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class WaygateKeyListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onWaygateKeyUse(WaygateKeyUseEvent useEvent) {
        Player p = useEvent.getPlayer();
        Action a = useEvent.getAction();
        Block b = useEvent.getClickedBlock();

        Gate gate = WaygateManager.getManager().getGateAtLocation(new BlockLocation(b.getLocation()));
        if (gate != null) {
            if (p.hasPermission("wg.key.use") && (a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR)) {
                if (gate.isOwnerPrivate() && !(gate.getOwner().equals(p.getUniqueId()) || p.hasPermission("wg.bypass")
                        || useEvent.isLockedKey())) {
                    Msg.GATE_ACCESS_DENIED.sendTo(p);
                } else {
                    // Give it a tick delay as opening a menu straight away can interfere with offhand placement
                    Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.getPluginInstance(), () ->
                            new MenuManager(p, gate).openWaygateMenu(), 1L);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryInteract(InventoryClickEvent clickEvent) {
        DataManager dm = DataManager.getManager();
        InventoryType type = clickEvent.getInventory().getType();
        InventoryAction a = clickEvent.getAction();

        if (dm.WG_KEY_PERMANENT && clickEvent.getWhoClicked().hasPermission("wg.key.permanent")) {
            if (!(type == InventoryType.PLAYER || type == InventoryType.CREATIVE || type == InventoryType.CRAFTING)) {
                if (clickEvent.getCurrentItem() != null && clickEvent.getCurrentItem().isSimilar(dm.WAYGATE_KEY))
                    clickEvent.setCancelled(true);

                if (a == InventoryAction.HOTBAR_MOVE_AND_READD || a == InventoryAction.HOTBAR_SWAP) {
                    ItemStack is = clickEvent.getView().getBottomInventory().getItem(clickEvent.getHotbarButton());

                    if (is != null && is.isSimilar(dm.WAYGATE_KEY))
                        clickEvent.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent dropEvent) {
        DataManager dm = DataManager.getManager();

        if (dm.WG_KEY_PERMANENT && dropEvent.getPlayer().hasPermission("wg.key.permanent"))
            if (dropEvent.getItemDrop().getItemStack().isSimilar(dm.WAYGATE_KEY))
                dropEvent.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent deathEvent) {
        DataManager dm = DataManager.getManager();

        if (dm.WG_KEY_PERMANENT && deathEvent.getEntity().hasPermission("wg.key.permanent")) {
            deathEvent.getDrops().removeIf(itemStack -> itemStack.isSimilar(dm.WAYGATE_KEY));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent joinEvent) {
        giveKey(joinEvent.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent respawnEvent) {
        giveKey(respawnEvent.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent changeEvent) {
        giveKey(changeEvent.getPlayer());
    }

    public void giveKey(Player p) {
        DataManager dm = DataManager.getManager();

        // If WG_KEY_PERMANENT is true, gives players with
        // "wg.key.permanent" a Waygate Key if they don't have one
        if (dm.WG_KEY_PERMANENT && p.hasPermission("wg.key.permanent")) {
            PlayerInventory inv = p.getInventory();

            if (!inv.containsAtLeast(dm.WAYGATE_KEY, 1)) {
                int emptySlot = inv.firstEmpty();

                if (emptySlot > -1)
                    inv.setItem(emptySlot, dm.WAYGATE_KEY);
            }
        }
    }

}
