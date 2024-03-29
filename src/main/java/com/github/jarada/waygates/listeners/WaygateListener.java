package com.github.jarada.waygates.listeners;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.*;
import com.github.jarada.waygates.events.WaygateInteractEvent;
import com.github.jarada.waygates.types.GateActivationResult;
import com.github.jarada.waygates.util.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class WaygateListener implements Listener {

    private final DataManager dm;
    private final WaygateManager gm;
    private final HashMap<Player, Location> playerLocationAtEvent = new HashMap<>();

    public WaygateListener() {
        dm = DataManager.getManager();
        gm = WaygateManager.getManager();
    }

    /* Gate Integrity */

    public void verifyGateIntegrity(Player p, Block block) {
        BlockLocation blockLocation = new BlockLocation(block.getLocation());

        // If broken block is part of a gate
        Gate gate = gm.getGateAtLocation(blockLocation);
        if (gate != null) {
            // It is destroyed
            gm.destroyWaygate(p, gate, blockLocation);
        }

        // If broken block is a controller
        Controller controller = gm.getControllerAtLocation(blockLocation);
        if (controller != null) {
            // It is destroyed
            gm.destroyController(p, controller);
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

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        // If placed block would be added to a gate
        BlockLocation blockLocation = new BlockLocation(e.getBlockPlaced().getLocation());
        Gate gate = gm.getGateAtLocation(blockLocation);
        if (gate != null) {
            // Prevent it
            e.setCancelled(true);
        }
    }

    /* Disable Vanilla Portal Behaviour */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerPortalEvent(PlayerPortalEvent e) {
        BlockLocation playerLocation = (playerLocationAtEvent.containsKey(e.getPlayer())) ?
                new BlockLocation(playerLocationAtEvent.get(e.getPlayer())) :
                new BlockLocation(e.getTo());
        if (gm.isGateNearby(playerLocation)) {
            Player p = e.getPlayer();
            e.setCancelled(true);

            // Verify Gate
            Gate gate = gm.getGateAtLocation(playerLocation);
            if (gate == null || !gate.verify(p))
                return;

            // Transport!
            gate.teleport(p);
            dm.saveWaygate(gate, false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityPortalEvent(EntityPortalEvent e) {
        BlockLocation entityLocation = new BlockLocation(e.getEntity().getLocation());
        if (gm.isGateNearby(entityLocation)) {
            e.setCancelled(true);

            // Verify Gate
            Gate gate = gm.getGateAtLocation(entityLocation);
            if (gate == null || !gate.isActive())
                return;

            // Transport!
            gate.teleportEntity(e.getEntity());
            dm.saveWaygate(gate, false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityPortalEnterEvent(EntityPortalEnterEvent e) {
        if (e.getEntity() instanceof Player) {
            playerLocationAtEvent.put((Player)e.getEntity(), e.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCreatureSpawnEvent(CreatureSpawnEvent e) {
        // Verify Spawn
        if (Arrays.stream(EntityType.values()).anyMatch((t) -> t.name().equals("PIG_ZOMBIE"))) {
            // 1.15- Support
            if (e.getEntityType() != EntityType.valueOf("PIG_ZOMBIE"))
                return;
        } else {
            if (e.getEntityType() != EntityType.ZOMBIFIED_PIGLIN)
                return;
        }

        if (e.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NETHER_PORTAL)
            return;

        // Verify Gate
        if (!gm.isGateNearby(new BlockLocation(e.getLocation()), 3))
            return;

        // Check Settings
        if (dm.WG_ZOMBIE_PIGMAN_ALLOWED)
            return;

        // Cancel
        e.setCancelled(true);
    }

    /* Gate Modification */

    @EventHandler(ignoreCancelled = true)
    public void onWaygateInteract(WaygateInteractEvent event) {
        if ((event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) ||
                event.getItem() == null)
            return;

        DataManager dm = DataManager.getManager();
        Player player = event.getPlayer();
        Gate gate = event.getWaygate();
        ItemStack is = event.getItem();
        boolean mainHand = is != null && is.equals(player.getInventory().getItemInMainHand());

        if (!mainHand)
            return;

        // Check to see if we have an icon listener for this player
        Optional<IconListener> listener = gate.getIconListenerForPlayer(player);
        if (listener.isPresent()) {
            listener.get().notify(player, is.getType());
            return;
        }
        
        // Only owner can modify gate
        if (gate.getOwner().equals(player.getUniqueId()) || player.hasPermission("wg.admin")) {
            // Check
            Material m = is.getType();
            String toSend;
            Object previous;
            if (m == Material.WRITTEN_BOOK) {
                BookMeta bm = (BookMeta) is.getItemMeta();

                if (bm == null || bm.hasDisplayName() || bm.hasLore())
                    return;

                StringBuilder content = new StringBuilder();

                for (int page = 1; page <= bm.getPageCount(); page++) {
                    content.append(bm.getPage(page));

                    if (page != bm.getPageCount())
                        content.append(" ");
                }

                if (content.length() > dm.WG_DESC_MAX_LENGTH)
                    content = new StringBuilder(content.substring(0, dm.WG_DESC_MAX_LENGTH));

                player.closeInventory();
                previous = gate.getDescription();
                gate.setDescription(content.toString());
                toSend = Msg.GATE_DESC_UPDATED_BOOK.toString(gate.getName(), bm.getTitle());
            } else {
                if (is.hasItemMeta())
                    return;

                previous = gate.getIcon();
                gate.setIcon(m);
                toSend = Msg.GATE_SET_ICON.toString(gate.getName(), m.toString());
            }

            boolean gateChanged = true;
            // If icon changes consume
            if (dm.WG_GATE_ICON_CHANGE_CONSUMES) {
                // Check for item duplication but during lag
                if (is.equals(player.getInventory().getItemInMainHand())) {
                    is.setAmount(is.getAmount() - 1);
                    player.getInventory().setItemInMainHand(is);
                } else {
                    gateChanged = false;
                    if (previous != null) {
                        if (previous.getClass().equals(Material.class)) {
                            gate.setIcon((Material) previous);
                        } else {
                            gate.setDescription((String) previous);
                        }
                    } else {
                        gate.setDescription(null);
                    }

                    WaygateManager wm = WaygateManager.getManager();
                    PluginMain pm = PluginMain.getPluginInstance();
                    wm.destroyWaygate(player, gate, new BlockLocation(event.getClickedBlock().getLocation()));
                    pm.getLogger().warning(String.format("Player %s no longer has item in main hand as expected; " +
                            "possible manipulation during lag", player.getName()));
                }
            }
            if (gateChanged) {
                dm.saveWaygate(gate, false);
                player.sendMessage(Util.color(toSend));
                Util.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
            }

        } else {
            Msg.NO_PERMS.sendTo(player);
        }
    }

    /* Gate Activation */

    @EventHandler
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        BlockLocation blockLocation = new BlockLocation(event.getBlock().getLocation());
        List<Gate> gates = gm.getGatesNearLocation(blockLocation, 1);

        if (event.getNewCurrent() > 0)
            for (Gate gate : gates) {
                if (gate != null && gate.getFixedDestination() != null && !gate.isActive()) {
                    // If gate is private or destination hidden, we only activate if the correct owners are present
                    if (gate.isOwnerPrivate() || gate.getFixedDestination().isOwnerHidden()) {
                        // Get list of nearby players, distance 1, to the event
                        List<Player> nearbyPlayers = Util.getNearbyPlayers(blockLocation.getLocation(), 1);
                        if ((gate.isOwnerPrivate() && nearbyPlayers.stream().noneMatch(o -> o.getUniqueId().equals(gate.getOwner()))) ||
                                (gate.getFixedDestination().isOwnerHidden() && nearbyPlayers.stream().noneMatch(o -> o.getUniqueId().equals(gate.getFixedDestination().getOwner()))))
                            return;
                    }

                    DataManager dm = DataManager.getManager();
                    WaygateManager wm = WaygateManager.getManager();
                    GateActivationResult result = gate.activate(gate.getFixedDestination());

                    if (result == GateActivationResult.RESULT_ACTIVATED)
                        dm.saveWaygate(gate, false);
                    else if (result == GateActivationResult.RESULT_NOT_INTACT)
                        wm.destroyWaygate(gate);
                }
            }
    }

    /* Gate Effects */

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        WaygateManager wm = WaygateManager.getManager();
        List<Gate> gates = wm.getGatesInChunk(e.getChunk());
        for (Gate gate : gates) {
            gate.getActivationEffect().loadChunk(gate);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        WaygateManager wm = WaygateManager.getManager();
        List<Gate> gates = wm.getGatesInChunk(e.getChunk());
        for (Gate gate : gates) {
            gate.getActivationEffect().unloadChunk(gate);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockWaterMove(BlockFromToEvent e) {
        if (e.getBlock().getType() == Material.WATER) {
            // If water block is spreading in gate
            BlockLocation blockLocation = new BlockLocation(e.getBlock().getLocation());
            Gate gate = gm.getGateAtLocation(blockLocation);
            if (gate != null) {
                // Prevent it
                e.setCancelled(true);
            }
        }
    }

    /* Clearance */

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerLocationAtEvent.remove(e.getPlayer());
    }
}
