package com.github.jarada.waygates.listeners;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.BlockLocation;
import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.util.Util;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class WaygateListener implements Listener {

    DataManager dm;
    WaygateManager gm;

    public WaygateListener() {
        dm = DataManager.getManager();
        gm = WaygateManager.getManager();
    }

    /* Gate Integrity */

    public void verifyGateIntegrity(Player p, Block block) {
        // If broken block is part of a gate
        BlockLocation blockLocation = new BlockLocation(block.getLocation());
        Gate gate = gm.getGateAtLocation(blockLocation);
        if (gate != null) {
            // It is destroyed
            gm.destroyWaygate(p, gate, blockLocation);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        verifyGateIntegrity(e.getPlayer(), e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        verifyGateIntegrity(Util.isPlayer(e.getEntity()) ? (Player)e.getEntity() : null, e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        for (Block block : e.blockList())
            verifyGateIntegrity(Util.isPlayer(e.getEntity()) ? (Player)e.getEntity() : null, block);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent e) {
        verifyGateIntegrity(null, e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent e) {
        verifyGateIntegrity(null, e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFadeEvent(BlockFadeEvent e) {
        verifyGateIntegrity(null, e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurnEvent(BlockBurnEvent e) {
        verifyGateIntegrity(null, e.getBlock());
    }

    /* Disable Vanilla Portal Behaviour */

    @EventHandler
    public void onPlayerPortalEvent(PlayerPortalEvent e) {
        if (gm.isGateNearby(new BlockLocation(e.getFrom()))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPortalEvent(EntityPortalEvent e) {
        if (gm.isGateNearby(new BlockLocation(e.getFrom()))) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCreatureSpawnEvent(CreatureSpawnEvent e) {
        // Verify Spawn
        if (e.getEntityType() != EntityType.PIG_ZOMBIE)
            return;

        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NETHER_PORTAL)
            return;

        // Verify Gate
        if (!gm.isGateNearby(new BlockLocation(e.getLocation())))
            return;

        // Check Settings
        // TODO Future: Pigman Spawn Setting

        // Cancel
        e.setCancelled(true);
    }

}
