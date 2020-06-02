package com.github.jarada.waygates.data;

import com.github.jarada.waygates.types.NetworkType;
import com.github.jarada.waygates.util.Util;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Network {

    private static Gson gson;

    private static Network       voidNetwork;
    private static Network       overworldNetwork;
    private static Network       netherNetwork;
    private static Network       underworldNetwork;
    private static Network       theEndNetwork;
    private static Network       oceanNetwork;

    private UUID                 uuid;
    private transient String     sysUuid;

    private UUID                 owner;
    private String               name;
    private Material             icon;

    // Set one or another at creation
    // Private: Not shown in network list for selection, only Owner can add/use gates on network
    // Invite: Not shown in network list for selection, can only add gates using invite code from Owner
    // NB: Use direct invites to the Network NOT codes
    private NetworkType          networkType;
    private List<UUID>           invitedUsers;

    @SuppressWarnings("unused")
    public Network(String name) {
        this(name, NetworkType.GLOBAL);
    }

    public Network(String name, NetworkType networkType) {
        this.name = name;
        this.networkType = networkType;
    }

    public static Network getVoidNetwork() {
        if (voidNetwork == null) {
            voidNetwork = new Network(Msg.NETWORK_SYSTEM_NAME_VOID.toString(), NetworkType.SYSTEM_VOID);
            voidNetwork.icon = Material.SNOWBALL;
            voidNetwork.sysUuid = "sys_void";
        }
        return voidNetwork;
    }

    public static Network getOverworldNetwork() {
        if (overworldNetwork == null) {
            overworldNetwork = new Network(Msg.NETWORK_SYSTEM_NAME_OVERWORLD.toString(), NetworkType.SYSTEM);
            overworldNetwork.icon = Material.APPLE;
            overworldNetwork.sysUuid = "sys_overworld";
        }
        return overworldNetwork;
    }

    public static Network getOceanNetwork() {
        if (oceanNetwork == null) {
            oceanNetwork = new Network(Msg.NETWORK_SYSTEM_NAME_OCEAN.toString(), NetworkType.SYSTEM);
            oceanNetwork.icon = Material.HEART_OF_THE_SEA;
            oceanNetwork.sysUuid = "sys_ocean";
        }
        return oceanNetwork;
    }

    public static Network getNetherNetwork() {
        if (netherNetwork == null) {
            netherNetwork = new Network(Msg.NETWORK_SYSTEM_NAME_NETHER.toString(), NetworkType.SYSTEM);
            netherNetwork.icon = Material.FIRE_CHARGE;
            netherNetwork.sysUuid = "sys_nether";
        }
        return netherNetwork;
    }

    public static Network getUnderworldNetwork() {
        if (underworldNetwork == null) {
            underworldNetwork = new Network(Msg.NETWORK_SYSTEM_NAME_UNDERWORLD.toString(), NetworkType.SYSTEM);
            underworldNetwork.icon = Material.MAGMA_CREAM;
            underworldNetwork.sysUuid = "sys_underworld";
        }
        return underworldNetwork;
    }

    public static Network getTheEndNetwork() {
        if (theEndNetwork == null) {
            theEndNetwork = new Network(Msg.NETWORK_SYSTEM_NAME_THE_END.toString(), NetworkType.SYSTEM);
            theEndNetwork.icon = Material.ENDER_EYE;
            theEndNetwork.sysUuid = "sys_the_end";
        }
        return theEndNetwork;
    }

    public static Network getSystemNetworkFromSysUUID(String sysUuid) {
        if (sysUuid.equals("sys_void"))
            return getVoidNetwork();
        if (sysUuid.equals("sys_overworld"))
            return getOverworldNetwork();
        if (sysUuid.equals("sys_ocean"))
            return getOceanNetwork();
        if (sysUuid.equals("sys_nether"))
            return getNetherNetwork();
        if (sysUuid.equals("sys_underworld"))
            return getUnderworldNetwork();
        if (sysUuid.equals("sys_the_end"))
            return getTheEndNetwork();
        return null;
    }

    public static List<Network> systemNetworks() {
        return new ArrayList<>(
                Arrays.asList(getVoidNetwork(), getOverworldNetwork(), getOceanNetwork(),
                        getNetherNetwork(), getUnderworldNetwork(), getTheEndNetwork())
        );
    }

    public static boolean isAbleToCreateNetworks(Player owner) {
        for (NetworkType networkType : NetworkType.values()) {
            if (networkType == NetworkType.SYSTEM || networkType == NetworkType.SYSTEM_VOID)
                continue;
            if (owner.hasPermission(String.format("wg.create.network.%s", networkType.toString().toLowerCase())))
                return true;
        }
        return false;
    }

    public UUID getUUID() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        return uuid;
    }

    public String getSysUuid() {
        return sysUuid;
    }

    public String getSysKey() {
        return getSysUuid().replace("sys_", "");
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        if (!isSystem())
            this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!isSystem())
            this.name = name;
    }

    public Material getIcon() {
        if (icon == null) {
            if (isPrivate())
                return Material.RAIL;
            if (isInvite())
                return Material.DETECTOR_RAIL;
            if (isFixed())
                return Material.ACTIVATOR_RAIL;
            return Material.POWERED_RAIL;
        }
        return icon;
    }

    private void prepareInvitedUsers() {
        if (invitedUsers == null)
            invitedUsers = new ArrayList<>();
        else if (invitedUsers.size() == 0)
            invitedUsers = null;
    }

    public List<UUID> getInvitedUsers() {
        if (invitedUsers == null)
            return new ArrayList<>();
        return invitedUsers;
    }

    public void addInvitedUser(UUID invite) {
        prepareInvitedUsers();
        invitedUsers.add(invite);
    }

    public void removeInvitedUser(UUID invite) {
        if (invitedUsers != null) {
            invitedUsers.remove(invite);
            prepareInvitedUsers();
        }
    }

    public boolean isInvitedUser(UUID invite) {
        return getOwner().equals(invite) || (invitedUsers != null && invitedUsers.contains(invite));
    }

    public boolean isGateAbleToUseNetwork(Gate gate) {
        Player owner = Bukkit.getPlayer(gate.getOwner());
        if (owner == null)
            return false;

        return isGateAbleToUseNetwork(owner, gate);
    }

    public boolean isGateAbleToUseNetwork(Player owner, Gate gate) {
        // Same network, of course!
        if (gate.getNetwork().equals(this))
            return true;

        // System network, permission check
        if (isSystem() && !owner.hasPermission(String.format("wg.network.%s", getSysKey())))
            return false;

        // Global network, permission check
        if (isGlobal() && !(owner.hasPermission("wg.network.global") ||
                owner.hasPermission(String.format("wg.network.%s", Util.getKey(getName())))))
            return false;

        // Fixed network, permission check
        if (isFixed() && !(owner.hasPermission("wg.network.fixed") ||
                owner.hasPermission(String.format("wg.network.%s", Util.getKey(getName())))))
            return false;

        // Invite network, permission check
        if (isInvite() && !isInvitedUser(owner.getUniqueId()))
            return false;

        // Private network, permission check
        return !isPrivate() || getOwner().equals(owner.getUniqueId());
    }

    public boolean isPrivate() {
        return networkType == NetworkType.PRIVATE;
    }

    public boolean isInvite() {
        return networkType == NetworkType.INVITE;
    }

    public boolean isFixed() {
        return networkType == NetworkType.FIXED;
    }

    public boolean isSystem() {
        return networkType == NetworkType.SYSTEM || networkType == NetworkType.SYSTEM_VOID;
    }

    public boolean isGlobal() {
        return networkType == NetworkType.GLOBAL;
    }

    public boolean isVoid() { return networkType == NetworkType.SYSTEM_VOID; }

    public boolean canAssignHiddenToGates(UUID gateOwner) {
        return networkType != NetworkType.FIXED || isInvitedUser(gateOwner);
    }

    public void clearSystemNetworkStatus() {
        if (isSystem())
            networkType = NetworkType.GLOBAL;
    }

    /* Serialization */

    private static Gson getGson() {
        if (gson == null)
            gson = new Gson();
        return gson;
    }

    public static Network fromJson(String json) {
        return getGson().fromJson(json, Network.class);
    }

    public String toJson() {
        return getGson().toJson(this);
    }
}
