package com.github.jarada.waygates.listeners;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.BlockLocation;
import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.events.WaygateInteractEvent;
import com.github.jarada.waygates.events.WaygateKeyUseEvent;
import com.github.jarada.waygates.types.GateCreationResult;
import com.github.jarada.waygates.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final DataManager    dm;
    private final WaygateManager gm;

    public PlayerListener() {
        dm = DataManager.getManager();
        gm = WaygateManager.getManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        Action a = event.getAction();
        ItemStack is = event.getItem();
        boolean mainHand = is != null && is.equals(p.getInventory().getItemInMainHand());

        if (a == Action.PHYSICAL || p.hasMetadata("InMenu"))
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
            if (dm.WG_CONSTRUCTOR_CONSUMES && canConsume && !p.hasPermission("wg.admin")) {
                is.setAmount(is.getAmount() - 1);
                p.getInventory().setItemInMainHand(is);
            }
        } else if (mainHand && !p.isSneaking() && is.isSimilar(dm.WAYGATE_KEY)) {
            if (!p.hasPermission("wg.key.use"))
                return;

            // Use Gate Key
            Bukkit.getPluginManager().callEvent(new WaygateKeyUseEvent(p, a, event.getClickedBlock()));
            event.setCancelled(true);
        } else if (p.isSneaking() && (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            BlockLocation gateLocation = new BlockLocation(event.getClickedBlock().getLocation());
            Gate gate = WaygateManager.getManager().getGateAtLocation(gateLocation);
            if (gate != null) {
                Bukkit.getPluginManager().callEvent(new WaygateInteractEvent(p, gate, a, is));
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
                (e.getRecipe().getResult().isSimilar(dm.WAYGATE_KEY) && !p.hasPermission("wg.craft.key"))) {
                e.setCancelled(true);
                Msg.NO_PERMS.sendTo(p);
            }
        }
    }

}
