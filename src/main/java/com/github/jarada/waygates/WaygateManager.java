package com.github.jarada.waygates;

import com.github.jarada.waygates.data.*;
import com.github.jarada.waygates.types.GateOrientation;
import com.github.jarada.waygates.util.FloodUtil;
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

    private static WaygateManager       gm;
    private PluginMain                  pm;

    private Map<Network, List<Gate>>    gates;
    private Map<BlockLocation, Gate>    locationGateMap;
    private Map<String, List<Gate>>     worldGateMap;
    private List<String>                worldDeletion;

    public WaygateManager() {
        pm = PluginMain.getPluginInstance();
        gates = new LinkedHashMap<>();
        locationGateMap = new LinkedHashMap<>();
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
        if (!this.gates.containsKey(gate.getNetwork())) {
            this.gates.put(gate.getNetwork(), new ArrayList<>());
        }
        this.gates.get(gate.getNetwork()).add(gate);

        if (!this.worldGateMap.containsKey(gate.getWorldName())) {
            this.worldGateMap.put(gate.getWorldName(), new ArrayList<>());
        }
        this.worldGateMap.get(gate.getWorldName()).add(gate);
    }

    private void removeFromGates(Gate gate) {
        if (this.gates.containsKey(gate.getNetwork())) {
            this.gates.get(gate.getNetwork()).remove(gate);
            if (!gate.getNetwork().isSystem() && this.gates.get(gate.getNetwork()).size() == 0) {
                this.gates.remove(gate.getNetwork());
            }
        }
        if (this.worldGateMap.containsKey(gate.getWorldName())) {
            this.worldGateMap.get(gate.getWorldName()).remove(gate);
            if (this.worldGateMap.get(gate.getWorldName()).size() == 0) {
                this.worldGateMap.remove(gate.getWorldName());
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
        if (this.gates.keySet().size() == 0)
            for (Network systemNetwork : Network.systemNetworks())
                this.gates.put(systemNetwork, new ArrayList<>());
        for (Gate gate : gates) {
            recordGate(gate, true);
            gate.deactivate();
        }
        pm.getLogger().info(String.format("Loaded %d gates in %d world(s)", gates.size(), worldGateMap.keySet().size()));
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
        Iterator<Gate> gateIterator = getGatesInNetwork(prevNetwork).iterator();
        while (gateIterator.hasNext()) {
            Gate toClose = gateIterator.next();
            if (toClose.getActiveDestination().equals(gate.getExit())) {
                // Close!
                toClose.deactivate();
            }
            // TODO Clear Fixed Destination Gates
        }

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

    /* Gate Locating */

    public Gate getGateAtLocation(BlockLocation blockLocation) {
        if (this.locationGateMap.containsKey(blockLocation))
            return this.locationGateMap.get(blockLocation);
        return null;
    }

    public boolean isGateNearby(BlockLocation blockLocation) {
        final int radius = 3;
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

    /* Gate Getters */

    public ArrayList<Gate> getConnectedGates(Gate gate) {
        ArrayList<Gate> gates = new ArrayList<>(this.gates.get(gate.getNetwork()));
        gates.remove(gate);
        return gates;
    }

    public ArrayList<Gate> getAllGatesInWorld(String worldName, boolean accurate) {
        ArrayList<Gate> gates = new ArrayList<Gate>();
        if (accurate && worldGateMap.containsKey(worldName)) {
            gates.addAll(worldGateMap.get(worldName));
        } else if (!accurate) {
            for (String storedWorldName : worldGateMap.keySet()) {
                if (storedWorldName.equalsIgnoreCase(worldName)) {
                    gates.addAll(worldGateMap.get(storedWorldName));
                }
            }
        }
        return gates;
    }

    public ArrayList<Gate> getAllGates() {
        ArrayList<Gate> gates = new ArrayList<Gate>();
        for (List<Gate> networkGates : this.gates.values()) {
            gates.addAll(networkGates);
        }
        return gates;
    }

    /* Gate Networks */

    public List<Network> getCustomNetworks(Player owner, Gate currentGate) {
        ArrayList<Network> customNetworks = new ArrayList<>();
        for (Network network : gates.keySet()) {
            if (!network.isSystem() && (currentGate.getNetwork().equals(network) ||
                    ((!network.isNetworkInvite() || network.isInvitedUser(owner.getUniqueId())) &&
                    (!network.isNetworkPrivate() || network.getOwner().equals(owner.getUniqueId())) &&
                    (!network.isGlobal() || owner.hasPermission("wg.network.global") ||
                            owner.hasPermission(String.format("wg.network.%s", Util.getKey(network.getName())))))))
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

    public List<Gate> getGatesInNetwork(Network network) {
        if (this.gates.containsKey(network))
            return this.gates.get(network);
        return new ArrayList<>();
    }

    public boolean isNetworkNameUnique(String name) {
        String cleansedName = Util.stripColor(name).toLowerCase();
        for (Network network : Network.systemNetworks()) {
            if (cleansedName.equals(Util.stripColor(network.getName()).toLowerCase()))
                return false;
        }
        for (Network network : this.gates.keySet()) {
            if (network.isGlobal() && cleansedName.equals(Util.stripColor(network.getName()).toLowerCase()))
                return false;
        }
        return true;
    }

    /* Gate Manipulation */

    public boolean createWaygate(Player p, Block clickedBlock, BlockFace clickedFace) {
        BlockLocation blockLocation = new BlockLocation(clickedBlock.getLocation());

        // If gate already exists, bail out
        if (getGateAtLocation(blockLocation) != null) {
            Msg.GATE_ALREADY_EXISTS.sendTo(p);
            return false;
        }

        // Alright, let's check if we can actually make something out of this
        Block startBlock = clickedBlock.getRelative(clickedFace);
        Map.Entry<GateOrientation, Set<Block>> gateFloodInfo = FloodUtil.getFloodInfo(startBlock,
                DataManager.getManager().MAX_AREA);

        if (gateFloodInfo == null) {
            Msg.GATE_NO_FRAME.sendTo(p);
            return false;
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
            return false;
        }

        // Calculate Exit Location
        Location playerLoc = p.getLocation();
        playerLoc.setPitch(0F);
        playerLoc.setYaw(gateOrientation.getExitYaw(playerLoc, blocks.iterator().next().getLocation()));
        GridLocation exit = new GridLocation(playerLoc);

        // Calculate Coords
        Set<BlockLocation> coords = new HashSet<>();
        for (Block block : blocks) {
            coords.add(new BlockLocation(block.getLocation()));
        }

        // Create the Gate and Fill
        Gate gate = new Gate(p.getUniqueId(), coords, new BlockLocation(startBlock.getLocation()), exit);
        gate.deactivate();

        // Record Gate
        this.recordGate(gate, false);

        // Run Sound FX
        Util.playParticle(startBlock.getLocation(), Particle.REDSTONE, 10);
        Util.playEffect(startBlock.getLocation(), Effect.ENDER_SIGNAL);
        Util.playSound(startBlock.getLocation(), Sound.BLOCK_BEACON_ACTIVATE);
        // NB Add Gate Creation Sound FX (CG Stored in UGate)

        // Inform Player
        Msg.GATE_CREATED.sendTo(p);

        return true;
    }

    public void destroyWaygate(@Nullable Player p, @NotNull Gate gate, @NotNull BlockLocation destroyingBlock) {
        // Clear Waygate
        destroyWaygate(gate);

        // Run FX
        Util.playEffect(destroyingBlock.getLocation(), Effect.ENDER_SIGNAL);
        Util.playParticle(destroyingBlock.getLocation(), Particle.EXPLOSION_LARGE, 1);
        Util.playParticle(destroyingBlock.getLocation(), Particle.REDSTONE, 10);
        Util.playSound(destroyingBlock.getLocation(), Sound.ENTITY_GENERIC_EXPLODE);
        Util.playSound(destroyingBlock.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE);

        // Message
        if (p != null)
            Msg.GATE_DESTROYED.sendTo(p, gate.getName());
    }

    public void destroyWaygate(@NotNull Gate gate) {
        // Unrecord Gate
        unrecordGate(gate);

        // Close Gate
        gate.deactivate();

        // Clear Menus
        gate.closeActiveMenus();
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

}
