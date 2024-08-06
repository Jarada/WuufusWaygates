package com.github.jarada.waygates;

import com.github.jarada.waygates.data.*;
import com.github.jarada.waygates.types.GateCreationResult;
import com.github.jarada.waygates.types.GateMaxIndicator;
import com.github.jarada.waygates.types.GateOrientation;
import com.github.jarada.waygates.types.ParticleType;
import com.github.jarada.waygates.util.FloodUtil;
import com.github.jarada.waygates.util.GateUtil;
import com.github.jarada.waygates.util.MaterialCountUtil;
import com.github.jarada.waygates.util.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WaygateManager {

    private static WaygateManager             gm;
    private final PluginMain                  pm;

    private final Map<Network, List<Gate>>    gates;
    private final Map<BlockLocation, Gate>    locationGateMap;
    private final Map<BlockLocation, Gate>    imprintGateMap;
    private final Map<BlockLocation, Controller> locationControllerMap;
    private final Map<String, List<Gate>>      chunkGateMap;
    private final Map<String, List<Gate>>     playerGateMap;
    private final Map<String, List<Controller>> worldControllerMap;
    private final Map<String, List<Gate>>     worldGateMap;
    private final List<String>                worldDeletion;

    public WaygateManager() {
        pm = PluginMain.getPluginInstance();
        gates = new LinkedHashMap<>();
        locationGateMap = new LinkedHashMap<>();
        imprintGateMap = new LinkedHashMap<>();
        locationControllerMap = new LinkedHashMap<>();
        chunkGateMap = new LinkedHashMap<>();
        playerGateMap = new LinkedHashMap<>();
        worldControllerMap = new LinkedHashMap<>();
        worldGateMap = new LinkedHashMap<>();
        worldDeletion = new ArrayList<>();
    }

    public static WaygateManager getManager() {
        if (gm == null)
            gm = new WaygateManager();

        return gm;
    }

    /* Gate Recording */

    private void addToGates(Gate gate) {
        // Verify blocks
        List<Block> blocks = gate.getBlocks();
        if (blocks == null) {
            String warning = "Unable to load Gate %s because World '%s' no longer exists. " +
                    "To remove this warning please ensure the 'worlds' folder in the plugin folder only contains valid worlds.";
            pm.getLogger().warning(String.format(warning, gate.getName(), gate.getWorldName()));
            return;
        }

        // Add gate
        if (!this.gates.containsKey(gate.getNetwork())) {
            this.gates.put(gate.getNetwork(), new ArrayList<>());
        }
        this.gates.get(gate.getNetwork()).add(gate);

        if (!this.playerGateMap.containsKey(gate.getOwner().toString())) {
            this.playerGateMap.put(gate.getOwner().toString(), new ArrayList<>());
        }
        this.playerGateMap.get(gate.getOwner().toString()).add(gate);

        if (!this.worldGateMap.containsKey(gate.getWorldName())) {
            this.worldGateMap.put(gate.getWorldName(), new ArrayList<>());
        }
        this.worldGateMap.get(gate.getWorldName()).add(gate);

        for (Block block : blocks) {
            if (!this.chunkGateMap.containsKey(block.getChunk().toString())) {
                this.chunkGateMap.put(block.getChunk().toString(), new ArrayList<>());
            }
            if (!this.chunkGateMap.get(block.getChunk().toString()).contains(gate)) {
                this.chunkGateMap.get(block.getChunk().toString()).add(gate);
            }
        }
    }

    private void removeFromGates(Gate gate) {
        if (this.gates.containsKey(gate.getNetwork())) {
            this.gates.get(gate.getNetwork()).remove(gate);
            if (!gate.getNetwork().isSystem() && this.gates.get(gate.getNetwork()).isEmpty()) {
                this.gates.remove(gate.getNetwork());
            }
        }
        if (this.playerGateMap.containsKey(gate.getOwner().toString())) {
            this.playerGateMap.get(gate.getOwner().toString()).remove(gate);
            if (this.playerGateMap.get(gate.getOwner().toString()).isEmpty()) {
                this.playerGateMap.remove(gate.getOwner().toString());
            }
        }
        if (this.worldGateMap.containsKey(gate.getWorldName())) {
            this.worldGateMap.get(gate.getWorldName()).remove(gate);
            if (this.worldGateMap.get(gate.getWorldName()).isEmpty()) {
                this.worldGateMap.remove(gate.getWorldName());
            }
        }

        for (Block block : gate.getBlocks()) {
            if (this.chunkGateMap.containsKey(block.getChunk().toString()) &&
                    this.chunkGateMap.get(block.getChunk().toString()).contains(gate)) {
                this.chunkGateMap.get(block.getChunk().toString()).remove(gate);
                if (this.chunkGateMap.get(block.getChunk().toString()).isEmpty()) {
                    this.chunkGateMap.remove(block.getChunk().toString());
                }
            }
        }
    }

    private void recordGate(Gate gate, boolean loading) {
        // Record Against Network
        addToGates(gate);

        // Record Against Blocks
        Set<BlockLocation> blockLocations = gate.getCoords();
        for (BlockLocation blockLocation : blockLocations) {
            this.locationGateMap.put(blockLocation, gate);
        }

        // Save Gate to File
        if (!loading)
            DataManager.getManager().saveWaygate(gate, true);
    }

    private void unrecordGate(Gate gate) {
        // Remove Against Network
        removeFromGates(gate);

        // Remove Against Blocks
        Set<BlockLocation> blockLocations = gate.getCoords();
        for (BlockLocation blockLocation : blockLocations) {
            this.locationGateMap.remove(blockLocation);
        }

        // Remove from File
        DataManager.getManager().deleteWaygate(gate, !gate.getNetwork().isSystem() && !this.gates.containsKey(gate.getNetwork()));
    }

    public void loadGates(List<Gate> gates) {
        if (this.gates.isEmpty())
            for (Network systemNetwork : Network.systemNetworks())
                this.gates.put(systemNetwork, new ArrayList<>());
        for (Gate gate : gates) {
            recordGate(gate, true);
            if (!gate.activateOnLoad()) {
                gate.deactivate();
            }
        }
        pm.getLogger().info(String.format("Loaded %d gates in %d world(s)", getAllGates().size(),
                worldGateMap.keySet().size()));
    }

    public void changeGateNetwork(Gate gate, Network network, boolean saveNetwork) {
        // No change needed if the networks are the same!
        if (gate.getNetwork() == network)
            return;

        // Record old network
        Network prevNetwork = gate.getNetwork();

        // Remove from old network
        removeFromGates(gate);

        // Close gates active to this gate, network has changed
        closeGatesActiveToGate(gate, prevNetwork);

        // Adjust network
        gate.setNetwork(network);

        // Add to new network
        addToGates(gate);

        // Save to File
        DataManager.getManager().saveWaygate(gate, saveNetwork);

        // Clear old network if need be
        if (!prevNetwork.isSystem() && !this.gates.containsKey(prevNetwork))
            DataManager.getManager().deleteNetwork(prevNetwork);
    }

    private void closeGatesActiveToGate(Gate gate, Network network) {
        for (Gate toClose : getGatesInNetwork(network)) {
            if (toClose.isActive() && toClose.getActiveDestination().equals(gate)) {
                // Close!
                toClose.deactivate();
            }
            if (toClose.getFixedDestination() != null && toClose.getFixedDestination().equals(gate)) {
                // Remove destination
                toClose.setFixedDestination(null);
                DataManager.getManager().saveWaygate(toClose, false);
            }
        }
    }

    /* Gate Locating */

    public Gate getGateAtLocation(BlockLocation blockLocation) {
        if (this.locationGateMap.containsKey(blockLocation))
            return this.locationGateMap.get(blockLocation);
        return null;
    }

    @SuppressWarnings("unused")
    public List<Gate> getGatesNearLocation(BlockLocation blockLocation) {
        // Radius is maximum distance gate frame blocks can be clicked
        return getGatesNearLocation(blockLocation, 5);
    }

    public List<Gate> getGatesNearLocation(BlockLocation blockLocation, final int radius) {
        ArrayList<Gate> gatesList = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockLocation search = new BlockLocation(blockLocation.getWorldName(),
                            blockLocation.getX() + dx, blockLocation.getY() + dy, blockLocation.getZ() + dz);
                    Gate gate = getGateAtLocation(search);
                    if (gate != null && !gatesList.contains(gate))
                        gatesList.add(gate);
                }
            }
        }
        return sortedGates(gatesList);
    }

    public List<Gate> getGatesInChunk(Chunk chunk) {
        if (chunkGateMap.containsKey(chunk.toString())) {
            return sortedGates(chunkGateMap.get(chunk.toString()));
        }
        return Collections.emptyList();
    }

    public boolean isGateNearby(BlockLocation blockLocation) {
        return isGateNearby(blockLocation, 1);
    }

    public boolean isGateNearby(BlockLocation blockLocation, final int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockLocation search = new BlockLocation(blockLocation.getWorldName(),
                            blockLocation.getX() + dx, blockLocation.getY() + dy, blockLocation.getZ() + dz);
                    if (getGateAtLocation(search) != null)
                        return true;
                }
            }
        }
        return false;
    }

    /* Gate Player Permission Check */

    public GateMaxIndicator getGateMaxIndicatorForPlayer(Player p) {
        int count = (playerGateMap.containsKey(p.getUniqueId().toString())) ? playerGateMap.get(p.getUniqueId().toString()).size() : 0;
        int allowedGates = 0;

        // Permission Loop
        if (!p.hasPermission("wg.admin") && !p.isOp())
            for(int i = 100; i > 0; i--){
                if(p.hasPermission("wg.create.gate.amount." + i)){
                    allowedGates = i;
                    break;
                }
            }

        return new GateMaxIndicator(count, allowedGates);
    }

    /* Gate Getters */

    public List<Gate> getConnectedGates(Gate gate, boolean hiddenComparator) {
        ArrayList<Gate> gatesList = new ArrayList<>(this.gates.get(gate.getNetwork()));
        gatesList.remove(gate);
        return sortedGates(gatesList, gate.getActiveDestination(), hiddenComparator);
    }

    public List<Gate> getAllGatesInWorld(String worldName, boolean accurate) {
        ArrayList<Gate> gatesList = new ArrayList<>();
        if (accurate && worldGateMap.containsKey(worldName)) {
            gatesList.addAll(worldGateMap.get(worldName));
        } else if (!accurate) {
            for (Map.Entry<String, List<Gate>> storedWorldName : worldGateMap.entrySet()) {
                if (storedWorldName.getKey().equalsIgnoreCase(worldName)) {
                    gatesList.addAll(storedWorldName.getValue());
                }
            }
        }
        return sortedGates(gatesList);
    }

    public List<Gate> getAllGates() {
        ArrayList<Gate> gatesList = new ArrayList<>();
        for (List<Gate> networkGates : this.gates.values()) {
            gatesList.addAll(networkGates);
        }
        return sortedGates(gatesList);
    }

    public List<Gate> sortedGates(List<Gate> unsorted) {
        return sortedGates(unsorted, null, false);
    }

    public List<Gate> sortedGates(List<Gate> unsorted, Gate accessGate, final boolean hiddenComparator) {
        unsorted.sort((o1, o2) -> {
            if (accessGate != null) {
                // Put Access Gate First
                if (o1 == accessGate)
                    return -1;
                else if (o2 == accessGate)
                    return 1;
            }

            if (hiddenComparator) {
                boolean hiddenA = o1.isOwnerHidden();
                boolean hiddenB = o2.isOwnerHidden();

                // Prefer non-hidden gates to hidden ones
                if (hiddenA && !hiddenB)
                    return -1;
                else if (hiddenB && !hiddenA)
                    return 1;
            }
            
            return o1.getName().compareTo(o2.getName());
        });
        return unsorted;
    }

    /* Gate Networks */

    public List<Network> getCustomNetworks(Player owner, Gate currentGate) {
        ArrayList<Network> customNetworks = new ArrayList<>();
        for (Network network : gates.keySet()) {
            if (!network.isSystem() && network.isGateAbleToUseNetwork(owner, currentGate))
                customNetworks.add(network);
        }
        return customNetworks;
    }

    public int countOfGatesInNetwork(Player p, Network network, boolean includeHidden) {
        if (this.gates.containsKey(network)) {
            if (!includeHidden) {
                int count = 0;
                boolean canBypass = p.hasPermission("wg.bypass");
                for (Gate gate : this.gates.get(network)) {
                    boolean isOwner = gate.getOwner().equals(p.getUniqueId());
                    if (gate.isOwnerHidden() && !(isOwner || canBypass))
                        continue;
                    count += 1;
                }
                return count;
            }
            return this.gates.get(network).size();
        }
        return 0;
    }

    public int countOfLocalOwnedGatesInNetwork(Player p, Network network, BlockLocation location, int radius) {
        List<Gate> localGates = getGatesNearLocation(location, radius);
        if (this.gates.containsKey(network)) {
            int count = 0;
            boolean canBypass = p.hasPermission("wg.bypass");
            for (Gate gate : this.gates.get(network)) {
                if (!localGates.contains(gate) || (!gate.getOwner().equals(p.getUniqueId()) && !canBypass))
                    continue;
                count += 1;
            }
            return count;
        }
        return 0;
    }

    public List<Gate> getGatesInNetwork(Network network) {
        if (this.gates.containsKey(network))
            return new ArrayList<>(this.gates.get(network));
        return new ArrayList<>();
    }

    public boolean isNetworkNameUnique(String name) {
        String cleansedName = Util.stripColor(name).toLowerCase();
        for (Network network : Network.systemNetworks()) {
            if (cleansedName.equals(Util.stripColor(network.getName()).toLowerCase()))
                return false;
        }
        for (Network network : this.gates.keySet()) {
            if ((network.isGlobal() || network.isFixed()) &&
                    cleansedName.equals(Util.stripColor(network.getName()).toLowerCase()))
                return false;
        }
        return true;
    }

    /* Gate Manipulation */

    public GateCreationResult createWaygate(Player p, Block clickedBlock, BlockFace clickedFace) {
        BlockLocation blockLocation = new BlockLocation(clickedBlock.getLocation());

        // If gate already exists, bail out
        Gate existing = getGateAtLocation(blockLocation);
        if (existing != null) {
            if (!existing.getOwner().equals(p.getUniqueId()))
                Msg.GATE_ALREADY_EXISTS.sendTo(p);
            return GateCreationResult.RESULT_EXISTING_GATE_FOUND;
        }

        // Check if gates of radius 1 are found
        // This prevents gates being formed right next to each other
        // As well as clears a bug where gates form from a wall
        if (!getGatesNearLocation(blockLocation, 1).isEmpty())
            return GateCreationResult.RESULT_EXISTING_NEARBY_GATE_FOUND;

        // Check to see if we can make a gate from the permission set
        GateMaxIndicator gateMaxIndicator = getGateMaxIndicatorForPlayer(p);
        if (!gateMaxIndicator.canCreate()) {
            Msg.GATE_MAX_REACHED.sendTo(p, gateMaxIndicator.getAmountAllowed());
            return GateCreationResult.RESULT_MAX_GATES_REACHED;
        }

        // Check if gates of minimal distance radius are found
        // We do this after permissions check to ensure players don't spend time
        // trying to fit gates into a radius, only to realise they can't build any
        int minimalRadius = DataManager.getManager().WG_GATE_MINIMAL_DISTANCE;
        if (minimalRadius > 1 && !getGatesNearLocation(blockLocation, minimalRadius).isEmpty()) {
            Msg.GATE_TOO_CLOSE.sendTo(p, minimalRadius);
            return GateCreationResult.RESULT_EXISTING_NEARBY_GATE_FOUND;
        }

        // Alright, let's check if we can actually make something out of this
        Block startBlock = clickedBlock.getRelative(clickedFace);
        Map.Entry<GateOrientation, Set<Block>> gateFloodInfo = FloodUtil.getFloodInfo(startBlock,
                DataManager.getManager().MAX_AREA);

        if (gateFloodInfo == null) {
            Msg.GATE_NO_FRAME.sendTo(p);
            return GateCreationResult.RESULT_NO_FRAME;
        }

        GateOrientation gateOrientation = gateFloodInfo.getKey();
        Set<Block> blocks = gateFloodInfo.getValue();

        // Check for required blocks
        Map<Material, Integer> materialCounts = MaterialCountUtil.count(blocks);
        List<Map<Material, Integer>> requiredBlocks = DataManager.getManager().getBlocksRequired();

        boolean hasRequiredBlocks = false;
        for (Map<Material, Integer> groupedRequirement : requiredBlocks) {
            if ((MaterialCountUtil.has(materialCounts, groupedRequirement))) {
                hasRequiredBlocks = true;
                break;
            }
        }
        if (!hasRequiredBlocks) {
            Msg.GATE_MUST_CONTAIN.sendTo(p, MaterialCountUtil.desc(requiredBlocks));
            return GateCreationResult.RESULT_REQUIRES_KEY_BLOCKS;
        }

        // Calculate Exit Location
        GridLocation exit = GateUtil.getExitForGate(p.getLocation(), blocks.iterator().next().getLocation(), null, gateOrientation);

        // Calculate Coords
        Set<BlockLocation> coords = new HashSet<>();
        for (Block block : blocks) {
            coords.add(new BlockLocation(block.getLocation()));
        }

        // Create the Gate (perhaps from imprint) and Fill
        boolean imprint = false;
        Gate gate = retrieveCachedImprint(p, coords);
        if (gate != null)
            imprint = true;
        else
            gate = new Gate(p.getUniqueId(), coords, new BlockLocation(startBlock.getLocation()), exit,
                    DataManager.getManager().WG_GATE_DEFAULT_ALWAYS_ON);
        gate.deactivate();

        // Record Gate
        this.recordGate(gate, false);

        // Run Sound FX
        createEffect(startBlock);

        // Inform Player
        if (imprint)
            Msg.GATE_CREATED_IMPRINT.sendTo(p, gate.getName());
        else
            Msg.GATE_CREATED.sendTo(p);

        return GateCreationResult.RESULT_GATE_CREATED;
    }

    public boolean updateWaygateExit(@NotNull Player p, Block clickedBlock) {
        Gate gate = gm.getGateAtLocation(new BlockLocation(clickedBlock.getLocation()));

        if (gate.getOwner().equals(p.getUniqueId()) || p.hasPermission("wg.admin")) {
            // Close Gate if Open
            if (gate.isActive())
                gate.deactivate();

            // Move exit
            GridLocation exit = GateUtil.getExitForGate(p.getLocation(), gate.getStart().getLocation(), gate, null);
            gate.setExit(exit);

            // Save Update
            DataManager.getManager().saveWaygate(gate, false);

            // Send Message
            Msg.GATE_EXIT_UPDATED.sendTo(p, gate.getName());

            return true;
        }
        return false;
    }

    public void destroyWaygate(@Nullable Player p, @NotNull Gate gate, @NotNull BlockLocation destroyingBlock) {
        // Clear Waygate
        destroyWaygate(gate);

        // Run FX
        destroyEffect(destroyingBlock);

        // Message
        if (p != null)
            Msg.GATE_DESTROYED.sendTo(p, gate.getName());
    }

    public void destroyWaygate(@NotNull Gate gate) {
        // Unrecord Gate
        unrecordGate(gate);

        // Close Gate and any gates active to the gate
        gate.deactivate();
        closeGatesActiveToGate(gate, gate.getNetwork());

        // Clear Menus and Listeners
        gate.closeActiveMenus();
        gate.clearIconListeners();

        // Set Imprint
        cacheImprint(gate);
    }

    public boolean isWorldAwaitingDeletion(String worldName) {
        return worldDeletion.contains(worldName);
    }

    public void setWorldForDeletion(String worldName) {
        if (!worldDeletion.contains(worldName))
            worldDeletion.add(worldName);
    }

    public void clearWorldForDeletion(String worldName) {
        worldDeletion.remove(worldName);
    }

    /* Gate Imprinting */

    public void cacheImprint(Gate gate) {
        Set<BlockLocation> blockLocations = gate.getCoords();
        for (BlockLocation blockLocation : blockLocations) {
            this.imprintGateMap.put(blockLocation, gate);
        }
    }

    public Gate retrieveCachedImprint(Player p, Set<BlockLocation> blockLocations) {
        Optional<BlockLocation> validBlock = imprintGateMap.keySet().stream()
                .filter(blockLocations::contains).findAny();
        if (validBlock.isPresent()) {
            Gate gate = imprintGateMap.get(validBlock.get());
            if ((gate.getOwner().equals(p.getUniqueId()) || p.hasPermission("wg.admin")) &&
                    gate.getCoords().equals(blockLocations)) {
                gate.getCoords().forEach(imprintGateMap::remove);
                return gate;
            } else {
                gate.getCoords().forEach(imprintGateMap::remove);
            }
        }
        return null;
    }

    /* Controller Recording */

    private void addToControllers(Controller controller) {
        locationControllerMap.put(controller.getLocation(), controller);

        if (!this.worldControllerMap.containsKey(controller.getWorldName())) {
            this.worldControllerMap.put(controller.getWorldName(), new ArrayList<>());
        }
        this.worldControllerMap.get(controller.getWorldName()).add(controller);
    }

    private void removeFromControllers(Controller controller) {
        locationControllerMap.remove(controller.getLocation());

        if (this.worldControllerMap.containsKey(controller.getWorldName())) {
            this.worldControllerMap.get(controller.getWorldName()).remove(controller);
            if (this.worldControllerMap.get(controller.getWorldName()).isEmpty()) {
                this.worldControllerMap.remove(controller.getWorldName());
            }
        }
    }

    private void recordController(Controller controller, boolean loading) {
        // Add to Map
        addToControllers(controller);

        // Save Controller to File
        if (!loading)
            DataManager.getManager().saveController(controller);
    }

    private void unrecordController(Controller controller) {
        // Remove from Map
        removeFromControllers(controller);

        // Remove from File
        DataManager.getManager().deleteController(controller);
    }

    public void loadControllers(List<Controller> controllers) {
        for (Controller controller : controllers) {
            recordController(controller, true);
        }
        pm.getLogger().info(String.format("Loaded %d controllers in %d world(s)", locationControllerMap.size(),
                worldGateMap.keySet().size()));
    }

    public void destroyController(@Nullable Player p, @NotNull Controller controller) {
        // Clear Controller
        destroyController(controller);

        // Run FX
        destroyEffect(controller.getLocation());

        // Message
        if (p != null)
            Msg.CONTROLLER_DESTROYED.sendTo(p);
    }

    public void destroyController(@NotNull Controller controller) {
        // Unrecord Controller
        unrecordController(controller);

        // Clear Menus
        controller.closeActiveMenus();
    }

    /* Controller Locating */

    public Controller getControllerAtLocation(BlockLocation blockLocation) {
        if (this.locationControllerMap.containsKey(blockLocation))
            return this.locationControllerMap.get(blockLocation);
        return null;
    }

    /* Control Manipulation */

    public boolean createController(Player p, Block clickedBlock) {
        BlockLocation blockLocation = new BlockLocation(clickedBlock.getLocation());

        // If gate already exists, bail out
        Gate existingGate = getGateAtLocation(blockLocation);
        if (existingGate != null) {
            if (!existingGate.getOwner().equals(p.getUniqueId()))
                Msg.GATE_ALREADY_EXISTS.sendTo(p);
            return false;
        }

        // If controller already exists, bail out
        Controller existingController = getControllerAtLocation(blockLocation);
        if (existingController != null) {
            if (!existingController.getOwner().equals(p.getUniqueId()))
                Msg.CONTROLLER_ALREADY_EXISTS.sendTo(p);
            return false;
        }

        // Create Controller
        Controller controller = new Controller(p.getUniqueId(), blockLocation);

        // Record Controller
        this.recordController(controller, false);

        // Run Sound FX
        createEffect(blockLocation.getLocation().getBlock());

        // Inform Player
        Msg.CONTROLLER_CREATED.sendTo(p);
        return true;
    }

    /* Shared FX */

    private void createEffect(Block startBlock) {
        Util.playParticle(startBlock.getLocation(), ParticleType.REDSTONE.get(), 10);
        Util.playEffect(startBlock.getLocation(), Effect.ENDER_SIGNAL);
        Util.playSound(startBlock.getLocation(), Sound.BLOCK_BEACON_ACTIVATE);
        // NB Add Gate Creation Sound FX (CG Stored in UGate)
    }

    private void destroyEffect(BlockLocation destroyingBlock) {
        Util.playEffect(destroyingBlock.getLocation(), Effect.ENDER_SIGNAL);
        Util.playParticle(destroyingBlock.getLocation(), ParticleType.EXPLOSION.get(), 1);
        Util.playParticle(destroyingBlock.getLocation(), ParticleType.REDSTONE.get(), 10);
        Util.playSound(destroyingBlock.getLocation(), Sound.ENTITY_GENERIC_EXPLODE);
        Util.playSound(destroyingBlock.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE);
    }

}
