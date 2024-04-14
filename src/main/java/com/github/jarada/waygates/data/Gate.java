package com.github.jarada.waygates.data;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.data.json.*;
import com.github.jarada.waygates.listeners.IconListener;
import com.github.jarada.waygates.menus.Menu;
import com.github.jarada.waygates.types.GateActivationResult;
import com.github.jarada.waygates.types.GateOrientation;
import com.github.jarada.waygates.util.FloodUtil;
import com.github.jarada.waygates.util.Util;
import com.google.gson.*;
import org.bukkit.*;
import org.bukkit.block.Block;
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

    private transient Gate activeDestination;
    private String activeDestinationUuid;

    private Material icon;
    private GateActivationEffect activationEffect;
    private boolean ownerPrivate;
    private boolean ownerHidden;
    private boolean alwaysOn;
    
    private final long createdMillis;
    private long activatedMillis;
    private long usedMillis;

    private GridLocation exit;
    private final BlockLocation start;
    private final Set<BlockLocation> coords;

    private transient Set<Menu> activeMenus;
    private transient Set<IconListener> activeIconListeners;
    private transient GridLocation activeLocation;
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

    public Gate getActiveDestination() {
        return activeDestination;
    }

    public void setActiveDestination(Gate activeDestination) {
        this.activeDestination = activeDestination;
    }

    public String getActiveDestinationUuid() {
        if (activeDestinationUuid == null && getActiveDestination() != null)
            return getActiveDestination().getUUID().toString();
        return activeDestinationUuid;
    }

    public void setActiveDestinationUuid(String activeDestinationUuid) {
        this.activeDestinationUuid = activeDestinationUuid;
    }

    public Material getIcon() {
        if (icon == null)
            return Material.ENDER_PEARL;
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public GateActivationEffect getActivationEffect() {
        if (activationEffect == null)
            return GateActivationEffect.NETHER;
        return activationEffect;
    }

    public void setActivationEffect(GateActivationEffect activationEffect) {
        this.activationEffect = activationEffect;
    }

    public void loopActivationEffect() {
        if (isActive())
            close();
        if (activationEffect == null)
            setActivationEffect(getActivationEffect());
        setActivationEffect(activationEffect.next());
        if (isActive())
            open();
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

    public boolean isAlwaysOn() {
        return alwaysOn;
    }

    public void setAlwaysOn(boolean alwaysOn) {
        this.alwaysOn = alwaysOn;
        if (!alwaysOn && isActive())
            deactivate();
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

    public GridLocation getExit() {
        return exit;
    }

    public void setExit(GridLocation gridLocation) {
        if (gridLocation != null)
            exit = gridLocation;
    }

    @SuppressWarnings("unused")
    public BlockLocation getStart() {
        return start;
    }

    public Set<BlockLocation> getCoords() {
        return coords;
    }

    public GridLocation getActiveLocation() {
        return activeLocation;
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

    /* Icon Listeners */

    public void addIconListener(IconListener listener) {
        if (activeIconListeners == null)
            activeIconListeners = new HashSet<>();
        activeIconListeners.add(listener);
    }

    public void removeIconListener(IconListener listener) {
        if (activeIconListeners != null)
            activeIconListeners.remove(listener);
    }

    public Optional<IconListener> getIconListenerForPlayer(Player player) {
        if (activeIconListeners != null) {
            return activeIconListeners.stream()
                    .filter(x -> x.isForPlayer(player))
                    .findAny();
        }
        return Optional.empty();
    }

    public void clearIconListeners() {
        if (activeIconListeners != null) {
            for (IconListener listener : activeIconListeners)
                listener.expire();
        }
        activeIconListeners = null;
    }

    /* Transport */

    public GateActivationResult activate(Gate destination) {
        // Verify Intact
        if (!isIntact()) {
            return GateActivationResult.RESULT_NOT_INTACT;
        }

        // Verify Location
        if (!isDestinationAvailable(destination)) {
            return GateActivationResult.RESULT_NOT_FOUND;
        }

        activatedMillis = System.currentTimeMillis();
        setupActiveDestination(destination);
        open();

        if (!isAlwaysOn()) {
            long activationTime = (long) 20 * DataManager.getManager().WG_GATE_ACTIVATION_TIME;
            activeTask = Bukkit.getScheduler().runTaskLater(PluginMain.getPluginInstance(), () -> {
                activeTask = null;
                deactivate();
            }, activationTime);
        }
        return GateActivationResult.RESULT_ACTIVATED;
    }

    public boolean activateOnLoad() {
        if (isAlwaysOn() && getWorld() != null && getActiveDestination() != null &&
                isActiveDestinationAvailable()) {
            setupActiveDestination(getActiveDestination());
            open();
            return true;
        }
        return false;
    }

    public void deactivate() {
        deactivate(false);
    }

    public void reopen() {
        if (isActive()) {
            close();
            open();
        }
    }

    public void deactivate(boolean saveGate) {
        if (activeTask != null)
            activeTask.cancel();
        activeTask = null;
        setupActiveDestination(null);
        close();

        if (saveGate && isAlwaysOn())
            DataManager.getManager().saveWaygate(this, false);
    }

    private void setupActiveDestination(Gate destination) {
        activeDestination = destination;
        activeDestinationUuid = (destination != null && isAlwaysOn()) ? destination.getUUID().toString() : null;
        activeLocation = (destination != null) ? destination.getExit() : null;
    }

    private boolean isDestinationAvailable(Gate destination) {
        GridLocation location = destination.getExit();
        return location.getWorld() != null;
    }

    private boolean isActiveDestinationAvailable() {
        return isDestinationAvailable(getActiveDestination());
    }

    public boolean isInLoadedChunks() {
        for (Block block : getBlocks()) {
            if (!block.getChunk().isLoaded())
                return false;
        }
        return true;
    }

    public boolean verify(Player p) {
        // Verify Active
        if (!isActive())
            return false;

        // Verify Permission
        return p.hasPermission("wg.travel") && (DataManager.getManager().WG_PRIVATE_GATES_ALLOW_TRAVEL ||
                !isOwnerPrivate() || getOwner().equals(p.getUniqueId()) || p.hasPermission("wg.bypass"));
    }

    private BlockLocation preTeleportChecks() {
        // Record Time Usage
        usedMillis = System.currentTimeMillis();

        // Verify
        BlockLocation to = (activeLocation != null) ? activeLocation : exit;
        if (to.getWorld() == null) {
            // Abort! Abort!
            deactivate(true);
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
        getActivationEffect().playTeleportSound(p.getLocation());
        if (p.isInsideVehicle() && (vehicle instanceof LivingEntity || vehicle instanceof Boat)) {
            executeTeleportVehicle(to, vehicle);
        } else {
            p.teleport(to.getTeleportLocation());
            p.setFireTicks(0);
        }

        if (to != exit)
            getActivationEffect().playTeleportSound(activeLocation.getLocation());

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
            entity.teleport(to.getTeleportLocation());
            entity.setFireTicks(0);
        } else if (entity.getType() == EntityType.DROPPED_ITEM && activeDestination != null &&
                activeDestination.getActivationEffect() == GateActivationEffect.NETHER) {
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
        executeTeleportVehicle(to, vehicle);
    }

    private void executeTeleportVehicle(BlockLocation to, Entity vehicle) {
        final List<Entity> passengers = vehicle.getPassengers();

        // Teleport Vehicle
        vehicle.eject();
        if (!(vehicle instanceof Player)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.getPluginInstance(), () -> {
                for (Entity passenger : passengers) {
                    passenger.teleport(to.getTeleportLocation());
                    passenger.setFireTicks(0);
                }
            }, 0L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(PluginMain.getPluginInstance(), () -> {
                vehicle.teleport(to.getTeleportLocation());
                vehicle.setFireTicks(0);
                for (Entity passenger : passengers) {
                    vehicle.addPassenger(passenger);
                }
            }, 2L);
        }
    }

    public boolean isActive() {
        return activeLocation != null;
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

    public Axis getOrientation() {
        List<Block> blocks = this.getBlocks();
        if (blocks != null) {
            Block origin = blocks.get(0);
            for (int i = 1; i < blocks.size(); i++) {
                Block other = blocks.get(i);
                if (other.getLocation().getBlockX() != origin.getLocation().getBlockX()) return Axis.X;
                if (other.getLocation().getBlockZ() != origin.getLocation().getBlockZ()) return Axis.Z;
            }
        }
        return Axis.X;
    }

    private void open() {
        getActivationEffect().activateGate(this);
    }

    private void close() {
        getActivationEffect().deactivateGate(this);
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
