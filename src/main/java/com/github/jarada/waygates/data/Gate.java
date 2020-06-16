package com.github.jarada.waygates.data;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.data.json.*;
import com.github.jarada.waygates.menus.Menu;
import com.github.jarada.waygates.types.GateActivationResult;
import com.github.jarada.waygates.types.GateOrientation;
import com.github.jarada.waygates.util.FloodUtil;
import com.github.jarada.waygates.util.Util;
import com.google.gson.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitTask;
import java.util.*;

public class Gate {

    private static Gson gson;

    private UUID uuid;
    private UUID owner;
    private String name;
    private String description;

    private transient Network network;
    private String networkUuid;

    private transient Gate destination;
    private String destinationUuid;

    private Material icon;
    private boolean ownerPrivate, ownerHidden;
    private final long createdMillis;
    private long activatedMillis;
    private long usedMillis;

    private final BlockLocation start;
    private final GridLocation exit;
    private final Set<BlockLocation> coords;

    private transient Set<Menu> activeMenus;
    private transient GridLocation activeDestination;
    private transient BukkitTask activeTask;

    public Gate(UUID owner, Set<BlockLocation> coords, BlockLocation start, GridLocation exit) {
        this.owner = owner;
        this.coords = coords;
        this.start = start;
        this.exit = exit;

        this.name = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        this.network = Network.getVoidNetwork();
        this.createdMillis = System.currentTimeMillis();
    }

    /* Getters / Setters */

    public UUID getUUID() {
        if (uuid == null)
            uuid = UUID.randomUUID();

        return uuid;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public String getNetworkUuid() {
        if (networkUuid == null)
            networkUuid = getNetwork().getUUID().toString();
        return networkUuid;
    }

    @SuppressWarnings("unused")
    public void setNetworkUuid(String networkUuid) {
        this.networkUuid = networkUuid;
    }

    public Gate getFixedDestination() {
        return destination;
    }

    public void setFixedDestination(Gate destination) {
        this.destination = destination;
    }

    public String getDestinationUuid() {
        if (destinationUuid == null && getFixedDestination() != null)
            return getFixedDestination().getUUID().toString();
        return destinationUuid;
    }

    @SuppressWarnings("unused")
    public void setDestinationUuid(String destinationUuid) {
        this.destinationUuid = destinationUuid;
    }

    public Material getIcon() {
        if (icon == null)
            return Material.ENDER_PEARL;
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public boolean isOwnerPrivate() {
        return ownerPrivate;
    }

    public void setOwnerPrivate(boolean ownerPrivate) {
        this.ownerPrivate = ownerPrivate;
    }

    public boolean isOwnerHidden() {
        if (getNetwork().isFixed() && !getNetwork().canAssignHiddenToGates(getOwner()))
            return true;
        return ownerHidden;
    }

    public void setOwnerHidden(boolean ownerHidden) {
        this.ownerHidden = ownerHidden;
    }

    @SuppressWarnings("unused")
    public long getCreatedMillis() {
        return createdMillis;
    }

    @SuppressWarnings("unused")
    public long getActivatedMillis() {
        return activatedMillis;
    }

    @SuppressWarnings("unused")
    public long getUsedMillis() {
        return usedMillis;
    }

    @SuppressWarnings("unused")
    public BlockLocation getStart() {
        return start;
    }

    public GridLocation getExit() {
        return exit;
    }

    public Set<BlockLocation> getCoords() {
        return coords;
    }

    public GridLocation getActiveDestination() {
        return activeDestination;
    }

    public String getWorldName() {
        return exit.getWorldName();
    }

    @SuppressWarnings("unused")
    public World getWorld() {
        return exit.getWorld();
    }

    /* Menus */

    public void addActiveMenu(Menu menu) {
        if (activeMenus == null)
            activeMenus = new HashSet<>();
        activeMenus.add(menu);
    }

    public void removeActiveMenu(Menu menu) {
        if (activeMenus != null)
            activeMenus.remove(menu);
    }

    public void closeActiveMenus() {
        if (activeMenus != null) {
            for (Menu activeMenu : activeMenus)
                activeMenu.close();
        }
    }

    /* Transport */

    public GateActivationResult activate(GridLocation location) {
        // Verify Intact
        if (!isIntact()) {
            return GateActivationResult.RESULT_NOT_INTACT;
        }

        // Verify Location
        if (location.getWorld() == null) {
            return GateActivationResult.RESULT_NOT_FOUND;
        }

        activeDestination = location;
        activatedMillis = System.currentTimeMillis();
        open();

        long activationTime = 20 * DataManager.getManager().WG_GATE_ACTIVATION_TIME;
        activeTask = Bukkit.getScheduler().runTaskLater(PluginMain.getPluginInstance(), () -> {
            activeTask = null;
            activeDestination = null;
            close();
        }, activationTime);
        return GateActivationResult.RESULT_ACTIVATED;
    }

    public void deactivate() {
        if (activeTask != null)
            activeTask.cancel();
        activeTask = null;
        activeDestination = null;
        close();
    }

    public boolean verify(Player p) {
        // Verify Active
        if (!isActive())
            return false;

        // Verify Permission
        return p.hasPermission("wg.travel") && (!isOwnerPrivate() || getOwner().equals(p.getUniqueId()) ||
                p.hasPermission("wg.bypass"));
    }

    private BlockLocation preTeleportChecks() {
        // Record Time Usage
        usedMillis = System.currentTimeMillis();

        // Verify
        BlockLocation to = (activeDestination != null) ? activeDestination : exit;
        if (to.getWorld() == null) {
            // Abort! Abort!
            deactivate();
            return null;
        }
        Util.checkChunkLoad(to.getWorld().getBlockAt(to.getLocation()));
        return to;
    }

    public void teleport(Player p) {
        // Gather Attached Entities
        final List<LivingEntity> leashed = getLeashed(p);
        final Entity vehicle = p.getVehicle();

        // Perform Pre Teleport checks
        BlockLocation to = preTeleportChecks();
        if (to == null) {
            Msg.GATE_EXIT_FAILURE.sendTo(p);
            return;
        }

        // Teleport Player
        Util.playSound(p.getLocation(), Sound.ENTITY_GHAST_SHOOT);
        if (p.isInsideVehicle() && vehicle instanceof LivingEntity) {
            vehicle.eject();
            if (!(vehicle instanceof Player)) {
                vehicle.teleport(to.getLocation());
                vehicle.setFireTicks(0);
                Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.getPluginInstance(), () -> vehicle.addPassenger(p), 2L);

            }
        } else {
            p.teleport(to.getLocation());
            p.setFireTicks(0);
        }

        if (to != exit)
            Util.playSound(activeDestination.getLocation(), Sound.ENTITY_GHAST_SHOOT);

        // Include Leashed Entities
        for (LivingEntity leashedEntity : leashed)
            teleportLeashed(leashedEntity, p);
    }

    public void teleportEntity(Entity entity) {
        // Perform Pre Teleport checks
        BlockLocation to = preTeleportChecks();
        if (to == null) {
            return;
        }

        if (entity.getType().isSpawnable()) {
            entity.teleport(to.getLocation());
        } else if (entity.getType() == EntityType.DROPPED_ITEM) {
            World world = to.getLocation().getWorld();
            if (world != null) {
                entity.remove();
                world.dropItemNaturally(to.getLocation(), ((Item) entity).getItemStack());
            }
        }
    }

    public void teleportVehicle(Vehicle vehicle) {
        // Gather Passengers
        final List<Entity> passengers = vehicle.getPassengers();
        final List<Player> players = new ArrayList<>();
        for (Entity p : passengers) {
            if (p instanceof Player) {
                players.add((Player)p);
            }
        }

        // Perform Pre Teleport checks
        BlockLocation to = preTeleportChecks();
        if (to == null) {
            for (Player p : players)
                Msg.GATE_EXIT_FAILURE.sendTo(p);
            return;
        }

        // Teleport Vehicle
        vehicle.eject();
        if (!(vehicle instanceof Player)) {
            vehicle.teleport(to.getLocation());
            vehicle.setFireTicks(0);
            Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.getPluginInstance(), () -> {
                for (Entity passenger : passengers) {
                    passenger.teleport(to.getLocation());
                    passenger.setFireTicks(0);
                }
            }, 0L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.getPluginInstance(), () -> {
                for (Entity passenger : passengers)
                    vehicle.addPassenger(passenger);
            }, 2L);
        }
    }

    public boolean isActive() {
        return activeDestination != null;
    }

    /* Leashed Transport */

    private List<LivingEntity> getLeashed(Player player) {
        List<LivingEntity> animals = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;
                try {
                    if (livingEntity.getLeashHolder().equals(player)) {
                        animals.add(livingEntity);
                    }
                } catch (IllegalStateException ignored) { }
            }
        }
        return animals;
    }

    private void teleportLeashed(LivingEntity livingEntity, Player player) {
        livingEntity.setLeashHolder(null);
        livingEntity.teleport(player);
        UUID entityUUID = livingEntity.getUniqueId();
        Bukkit.getScheduler().runTaskLater(PluginMain.getPluginInstance(), () -> {
            for (Entity aroundEntity : player.getNearbyEntities(15, 15, 15)) {
                if (aroundEntity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) aroundEntity;
                    if (aroundEntity.getUniqueId().equals(entityUUID)) {
                        aroundEntity.teleport(player);
                        living.setLeashHolder(player);
                        break;
                    }
                }
            }
        }, 3L);
    }

    /* Gate Makeup */

    // These blocks are sorted since the coords are sorted
    public List<Block> getBlocks()
    {
        List<Block> ret = new ArrayList<>();

        World world;
        try
        {
            world = this.getExit().getWorld();
        }
        catch (IllegalStateException e)
        {
            return null;
        }
        if (world == null)
            return null;

        for (BlockLocation coord : this.getCoords())
        {
            Block block = world.getBlockAt(coord.getX(), coord.getY(), coord.getZ());
            ret.add(block);
        }

        return ret;
    }

    public Block getCenterBlock()
    {
        List<Block> blocks = this.getBlocks();
        if (blocks == null) return null;

        return blocks.get(blocks.size() / 2);
    }

    public boolean isIntact()
    {
        if (isActive())
            return true;

        // Get Block
        World world;
        try
        {
            world = start.getWorld();
        }
        catch (IllegalStateException e)
        {
            return false;
        }
        Block startBlock = world.getBlockAt(start.getX(), start.getY(), start.getZ());

        // Gather current makeup of Gate
        Map.Entry<GateOrientation, Set<Block>> gateFloodInfo = FloodUtil.getFloodInfo(startBlock,
                DataManager.getManager().MAX_AREA);
        if (gateFloodInfo == null) {
            return false;
        }

        // Calculate Coords
        Set<BlockLocation> currentCoords = new HashSet<>();
        for (Block block : gateFloodInfo.getValue()) {
            currentCoords.add(new BlockLocation(block.getLocation()));
        }
        
        return this.coords.equals(currentCoords);
    }

    public void setContent(Material material)
    {
        List<Block> blocks = this.getBlocks();
        if (blocks == null) return;
        Axis axis = Axis.X;

        // Orientation check
        if (material == Material.NETHER_PORTAL)
        {
            Block origin = blocks.get(0);
            Block blockSouth = origin.getRelative(BlockFace.SOUTH);
            Block blockNorth = origin.getRelative(BlockFace.NORTH);

            if (blocks.contains(blockNorth) || blocks.contains(blockSouth))
            {
                axis = Axis.Z;
            }
        }

        for (Block block : blocks)
        {
            Material blockMaterial = block.getType();

            if (blockMaterial != Material.NETHER_PORTAL && !Util.isMaterialAir(blockMaterial)) continue;

            block.setType(material);

            // Apply orientation
            if (material != Material.NETHER_PORTAL) continue;

            BlockData data = block.getBlockData();
            if (data instanceof Orientable) {
                ((Orientable) data).setAxis(axis);
                block.setBlockData(data);
            }
        }
    }

    private void open() {
        this.setContent(Material.NETHER_PORTAL);
    }

    private void close() {
        this.setContent(Material.AIR);
    }

    /* Serialization */

    private static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder().registerTypeAdapterFactory(new GateTypeAdapterFactory()).create();
        }
        return gson;
    }

    public static Gate fromJson(String json) {
        return getGson().fromJson(json, Gate.class);
    }

    public String toJson() {
        if (network.isSystem())
            networkUuid = network.getSysUuid();
        else
            networkUuid = network.getUUID().toString();
        if (destination != null)
            destinationUuid = destination.getUUID().toString();
        return getGson().toJson(this);
    }

}
