package com.github.jarada.waygates.data;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.data.json.*;
import com.github.jarada.waygates.types.GateOrientation;
import com.github.jarada.waygates.util.FloodUtil;
import com.github.jarada.waygates.util.Util;
import com.google.gson.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
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
    private long createdMillis, activatedMillis, usedMillis;

    private BlockLocation start;
    private GridLocation exit;
    private Set<BlockLocation> coords;

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

    public void setNetworkUuid(String networkUuid) {
        this.networkUuid = networkUuid;
    }

    public Gate getDestination() {
        return destination;
    }

    public void setDestination(Gate destination) {
        this.destination = destination;
    }

    public String getDestinationUuid() {
        if (destinationUuid == null && getDestination() != null)
            return getDestination().getUUID().toString();
        return destinationUuid;
    }

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
        return ownerHidden;
    }

    public void setOwnerHidden(boolean ownerHidden) {
        this.ownerHidden = ownerHidden;
    }

    public long getCreatedMillis() {
        return createdMillis;
    }

    public long getActivatedMillis() {
        return activatedMillis;
    }

    public long getUsedMillis() {
        return usedMillis;
    }

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

    public World getWorld() {
        return exit.getWorld();
    }

    /* Transport */

    public boolean activate(GridLocation location) {
        // Verify Intact
        if (!isIntact()) {
            return false;
        }

        activeDestination = location;
        activatedMillis = System.currentTimeMillis();
        open();

        long activationTime = 20 * DataManager.getManager().WG_GATE_ACTIVATION_TIME;
        activeTask = Bukkit.getScheduler().runTaskLater(PluginMain.getPluginInstance(), new Runnable() {

            @Override
            public void run() {
                activeTask = null;
                activeDestination = null;
                close();
            }

        }, activationTime);
        return true;
    }

    public void deactivate() {
        if (activeTask != null)
            activeTask.cancel();
        activeTask = null;
        activeDestination = null;
        close();
    }

    public void teleport(Player p) {
        usedMillis = System.currentTimeMillis();
        Util.playSound(p.getLocation(), Sound.ENTITY_GHAST_SHOOT);
        if (activeDestination != null) {
            p.teleport(activeDestination.getLocation());
            Util.playSound(activeDestination.getLocation(), Sound.ENTITY_GHAST_SHOOT);
        } else {
            p.teleport(exit.getLocation());
        }
    }

    public boolean isActive() {
        return activeDestination != null;
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

            if (blockMaterial != Material.NETHER_PORTAL && !blockMaterial.isAir()) continue;

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
