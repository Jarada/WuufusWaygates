package com.github.jarada.waygates.listeners;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.BlockLocation;
import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.events.WaygateKeyUseEvent;
import com.github.jarada.waygates.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private DataManager     dm;
    private WaygateManager gm;

    public PlayerListener() {
        dm = DataManager.getManager();
        gm = WaygateManager.getManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Action a = event.getAction();
        ItemStack is = event.getItem();

        if (a == Action.PHYSICAL || p.hasMetadata("InMenu"))
            return;

        if (is == null || event.getClickedBlock() == null)
            return;

        if (!p.isSneaking() && is.isSimilar(dm.WAYGATE_CONSTRUCTOR)) {
            if (!p.hasPermission("wg.create.gate"))
                return;

            // Attempt to create a gate
            boolean success = gm.createWaygate(p, event.getClickedBlock(), event.getBlockFace());
            event.setCancelled(true);
            if (dm.WG_CONSTRUCTOR_CONSUMES && success && !p.hasPermission("wg.admin")) {
                is.setAmount(is.getAmount() - 1);
                p.getInventory().setItemInMainHand(is);
            }
        } else if (!p.isSneaking() && is.isSimilar(dm.WAYGATE_KEY)) {
            if (!p.hasPermission("wg.key.use"))
                return;

            // Use Gate Key
            Bukkit.getPluginManager().callEvent(new WaygateKeyUseEvent(p, a, event.getClickedBlock()));
            event.setCancelled(true);
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
        if (gate == null)
            return;

        // Verify Active
        if (!gate.isActive())
            return;

        // Verify Permission
        if (!p.hasPermission("wg.travel") || (gate.isOwnerPrivate() && !gate.getOwner().equals(p.getUniqueId()) &&
                !p.hasPermission("wg.bypass")))
            return;

        // Transport!
        gate.teleport(p);
        dm.saveWaygate(gate, false);
    }

}