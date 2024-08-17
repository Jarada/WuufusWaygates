package com.github.jarada.waygates.listeners;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.*;
import com.github.jarada.waygates.events.WaygateInteractEvent;
import com.github.jarada.waygates.events.WaygateKeyUseEvent;
import com.github.jarada.waygates.types.GateCreationResult;
import com.github.jarada.waygates.util.Util;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final DataManager    dm;
    private final WaygateManager gm;
    private final PluginMain     pm;

    public PlayerListener() {
        dm = DataManager.getManager();
        gm = WaygateManager.getManager();
        pm = PluginMain.getPluginInstance();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        handleRecipeDiscovery(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        handleRecipeDiscovery(event.getPlayer());
    }

    // Undiscover on quit for two reasons:
    // 1. To allow players to rediscover recipes if they change while the player is offline.
    // 2. If the plugin is removed, Spigot will otherwise print errors about missing recipes.
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogout(PlayerQuitEvent event) {
        event.getPlayer().undiscoverRecipes(dm.getAllCraftableItems().values());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Action a = event.getAction();
        ItemStack is = event.getItem();
        boolean mainHand = is != null && is.equals(p.getInventory().getItemInMainHand());

        if (a == Action.PHYSICAL || p.hasMetadata("InMenu"))
            return;
        
        if (p.getInventory().getItemInMainHand().getType() == Material.AIR && checkPlayerEmptyHandControllerInteraction(event))
            return;

        if (is == null || event.getClickedBlock() == null)
            return;

        if (mainHand && !p.isSneaking() && is.isSimilar(dm.WAYGATE_CONSTRUCTOR)) {
            if (!p.hasPermission("wg.create.gate"))
                return;

            // Attempt to create a gate
            GateCreationResult result = gm.createWaygate(p, event.getClickedBlock(), event.getBlockFace());
            boolean canConsume = result == GateCreationResult.RESULT_GATE_CREATED;
            event.setCancelled(true);

            // Check if we need to move exit of a gate
            if (result == GateCreationResult.RESULT_EXISTING_GATE_FOUND) {
                canConsume = gm.updateWaygateExit(p, event.getClickedBlock());
            }

            // Consume Waygate Constructor
            if (dm.WG_CONSTRUCTOR_CONSUMES && canConsume && !p.hasPermission("wg.keep.constructor")) {
                is.setAmount(is.getAmount() - 1);
                p.getInventory().setItemInMainHand(is);
            }
        } else if (mainHand && !p.isSneaking() && is.isSimilar(dm.WAYGATE_CONTROL)) {
            if (!p.hasPermission("wg.create.control"))
                return;

            // Attempt to create a Controller
            boolean result = gm.createController(p, event.getClickedBlock());
            event.setCancelled(true);

            // Consume Waygate Control Creator
            if (dm.WG_CONTROL_CREATOR_CONSUMES && result && !p.hasPermission("wg.keep.control.creator")) {
                is.setAmount(is.getAmount() - 1);
                p.getInventory().setItemInMainHand(is);
            }
        } else if (mainHand && !p.isSneaking()) {
            if (!p.hasPermission("wg.key.use"))
                return;

            WaygateKeyUseEvent keyUseEvent = null;
            if (is.isSimilar(dm.WAYGATE_KEY)) {
                keyUseEvent = new WaygateKeyUseEvent(p, a, event.getClickedBlock());
            } else {
                // Verify Gate Availability and Lock
                Gate gate = WaygateManager.getManager().getGateAtLocation(new BlockLocation(event.getClickedBlock().getLocation()));
                if (gate != null && dm.isLockKeyValid(gate, is))
                    keyUseEvent = new WaygateKeyUseEvent(p, a, event.getClickedBlock()).withLockedKey();
                else {
                    // Verify Controller Availability and Lock
                    Controller controller = WaygateManager.getManager().getControllerAtLocation(new BlockLocation(event.getClickedBlock().getLocation()));
                    if (controller != null && controller.getGate() != null && dm.isLockKeyValid(controller.getGate(), is))
                        keyUseEvent = new WaygateKeyUseEvent(p, a, event.getClickedBlock()).withLockedKey();
                }
            }

            if (keyUseEvent != null) {
                // Use Gate Key
                Bukkit.getPluginManager().callEvent(keyUseEvent);
                event.setCancelled(true);
            }
        } else if (p.isSneaking() && (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            if (!p.hasPermission("wg.icon.change"))
                return;

            BlockLocation gateLocation = new BlockLocation(event.getClickedBlock().getLocation());
            Gate gate = WaygateManager.getManager().getGateAtLocation(gateLocation);
            if (gate != null) {
                Bukkit.getPluginManager().callEvent(new WaygateInteractEvent(p, gate, event.getClickedBlock(), a, is));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        // Verify Player
        Player p = e.getPlayer();
        if (!Util.isPlayer(p) || p.isDead())
            return;

        // Verify Movement
        BlockLocation from = new BlockLocation(e.getFrom());
        BlockLocation to = new BlockLocation(e.getTo());
        if (from.equals(to))
            return;

        // Verify Gate
        Gate gate = gm.getGateAtLocation(to);
        if (gate == null || !gate.verify(p))
            return;

        // Transport!
        gate.teleport(p);
        dm.saveWaygate(gate, false);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerCraft(CraftItemEvent e) {
        HumanEntity he = e.getWhoClicked();
        if (he instanceof Player) {
            // Verify Player
            Player p = (Player) he;
            if (!Util.isPlayer(p) || p.isDead())
                return;

            // Verify Recipe
            if ((e.getRecipe().getResult().isSimilar(dm.WAYGATE_CONSTRUCTOR) && !p.hasPermission("wg.craft.constructor")) ||
                (e.getRecipe().getResult().isSimilar(dm.WAYGATE_KEY) && !p.hasPermission("wg.craft.key")) ||
                (e.getRecipe().getResult().isSimilar(dm.WAYGATE_CONTROL) && !p.hasPermission("wg.craft.control.creator"))) {
                e.setCancelled(true);
                Msg.NO_PERMS.sendTo(p);
            }
        }
    }

    private boolean checkPlayerEmptyHandControllerInteraction(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Action a = event.getAction();
        EquipmentSlot h = event.getHand();

        if (h == EquipmentSlot.HAND && event.getClickedBlock() != null && !p.isSneaking()) {
            Controller controller = WaygateManager.getManager().getControllerAtLocation(new BlockLocation(event.getClickedBlock().getLocation()));
            if (controller != null) {
                Bukkit.getPluginManager().callEvent(new WaygateKeyUseEvent(p, a, event.getClickedBlock()).withEmptyHand());
                event.setCancelled(true);
                return true;
            }
        }
        return false;
    }

    private void handleRecipeDiscovery(Player p) {
        if (!dm.WG_GIVE_PLAYERS_RECIPES)
            return;

        ArrayList<NamespacedKey> toDiscover = new ArrayList<NamespacedKey>(3);
        ArrayList<NamespacedKey> toUndiscover = new ArrayList<NamespacedKey>(3);
        
        if (p.hasPermission("wg.craft.key")) toDiscover.add(dm.getCraftableItem(CraftableWaygateItem.WAYGATE_KEY));
        else toUndiscover.add(dm.getCraftableItem(CraftableWaygateItem.WAYGATE_KEY));

        if (p.hasPermission("wg.craft.constructor")) toDiscover.add(dm.getCraftableItem(CraftableWaygateItem.WAYGATE_CONSTRUCTOR));
        else toUndiscover.add(dm.getCraftableItem(CraftableWaygateItem.WAYGATE_CONSTRUCTOR));

        if (p.hasPermission("wg.craft.control.creator")) toDiscover.add(dm.getCraftableItem(CraftableWaygateItem.WAYGATE_CONTROL));
        else toUndiscover.add(dm.getCraftableItem(CraftableWaygateItem.WAYGATE_CONTROL));    

        if (!toUndiscover.isEmpty()) {
            int result = p.undiscoverRecipes(toUndiscover);
            if (result == 0)
                pm.getLogger().warning(String.format("Player \"%s\" (%s) failed to undiscover recipes.", p.getName(), p.getUniqueId()));
        }

        if (!toDiscover.isEmpty()) {
            int result = p.discoverRecipes(toDiscover);
            if (result == 0)
                pm.getLogger().warning(String.format("Player \"%s\" (%s) failed to discover recipes.", p.getName(), p.getUniqueId()));
        }
    }
}
