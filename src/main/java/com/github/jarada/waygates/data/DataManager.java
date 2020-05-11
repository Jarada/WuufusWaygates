package com.github.jarada.waygates.data;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.util.Util;
import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataManager {

    private static final String WORLDS_FOLDER_FILENAME = "worlds";
    private static final String NETWORKS_FOLDER_FILENAME = "networks";

    private static DataManager          dm;
    private PluginMain                  pm;
    private WaygateManager              wm;

    private File                        worldsFolder, networksFolder;
    private Map<Msg, String>            messages;

    public ItemStack                    WAYGATE_CONSTRUCTOR;
    public ItemStack                    WAYGATE_KEY;

    public int                          MAX_AREA;
    public List<Map<String, Integer>>   BLOCKS_REQUIRED;
    public int                          WG_NAME_MAX_LENGTH, WG_DESC_MAX_LENGTH;
    public int                          WG_NETWORK_NAME_MAX_LENGTH;
    public int                          WG_GATE_ACTIVATION_TIME;
    public boolean                      WG_CONSTRUCTOR_CONSUMES;
    public boolean                      WG_KEY_PERMANENT;

    public DataManager() {
        pm = PluginMain.getPluginInstance();
        wm = WaygateManager.getManager();

        messages = new HashMap<Msg, String>();
        worldsFolder = new File(pm.getDataFolder(), WORLDS_FOLDER_FILENAME);
        networksFolder = new File(pm.getDataFolder(), NETWORKS_FOLDER_FILENAME);
    }

    public static DataManager getManager() {
        if (dm == null)
            dm = new DataManager();

        return dm;
    }

    public void loadConfig(boolean reload) {
        if (!worldsFolder.exists())
            worldsFolder.mkdir();
        if (!networksFolder.exists())
            networksFolder.mkdir();

        pm.saveDefaultConfig();

        FileConfiguration config = pm.getConfig();
        for (Msg msg : Msg.values()) {
            String path = "Waygates.Messages." + msg.name();
            config.addDefault(path, msg.getDefaultMsg());
            messages.put(msg, config.getString(path));
        }

        MAX_AREA = config.getInt("Waygates.MAX_AREA");
        WG_NAME_MAX_LENGTH = Integer.max(6, config.getInt("Waygates.WG_NAME_MAX_LENGTH"));
        WG_DESC_MAX_LENGTH = config.getInt("Waygates.WG_DESC_MAX_LENGTH"); // TODO Future: Gate Description
        WG_NETWORK_NAME_MAX_LENGTH = Integer.max(6, config.getInt("Waygates.WG_NETWORK_NAME_MAX_LENGTH"));
        WG_GATE_ACTIVATION_TIME = Integer.min(300, Integer.max(5, config.getInt("Waygates.WG_GATE_ACTIVATION_TIME")));
        WG_CONSTRUCTOR_CONSUMES = config.getBoolean("Waygates.WG_CONSTRUCTOR_CONSUMES");
        WG_KEY_PERMANENT = config.getBoolean("Waygates.WG_KEY_PERMANENT"); // TODO Future: Permanent Key

        BLOCKS_REQUIRED = new ArrayList<>();
        try {
            for (String group : config.getConfigurationSection("Waygates.BLOCKS_REQUIRED").getKeys(false)) {
                HashMap<String, Integer> requirement = new HashMap<>();
                for (String inv : config.getConfigurationSection("Waygates.BLOCKS_REQUIRED." + group).getKeys(false)) {
                    requirement.put(inv, config.getInt("Waygates.BLOCKS_REQUIRED." + group + "." + inv));
                }
                BLOCKS_REQUIRED.add(requirement);
            }
        } catch (NullPointerException e) {
            // Using Static Default
            pm.getLogger().warning("Using Static Default for Blocks Required");
        }

        config.options().copyDefaults(true);
        pm.saveConfig();

        if (!reload) {
            registerGlow();
            Glow glow = new Glow(new NamespacedKey(pm, "waygateglow"));

            List<String> lore = new ArrayList<String>();
            Msg[] constructorLore = {Msg.LORE_CONSTRUCTOR_1, Msg.LORE_CONSTRUCTOR_2, Msg.LORE_CONSTRUCTOR_3, Msg.LORE_CONSTRUCTOR_4};
            for (Msg msg : constructorLore)
                if (msg.toString().length() > 0)
                    lore.add(Util.color(msg.toString()));

            WAYGATE_CONSTRUCTOR = Util.setItemNameAndLore(new ItemStack(Material.GOLD_NUGGET, 1), Msg.LORE_CONSTRUCTOR_NAME.toString(), lore);
            ItemMeta activatorMeta = WAYGATE_CONSTRUCTOR.getItemMeta();
            if (activatorMeta != null) {
                activatorMeta.addEnchant(glow, 1, true);
                WAYGATE_CONSTRUCTOR.setItemMeta(activatorMeta);
            }

            ShapedRecipe sr = new ShapedRecipe(new NamespacedKey(pm, "waygateconstructor"), WAYGATE_CONSTRUCTOR);
            sr.shape("RRR", "RGR", "RRR").setIngredient('R', Material.REDSTONE).setIngredient('G', Material.GOLD_NUGGET);
            Bukkit.addRecipe(sr);

            lore = new ArrayList<>();
            Msg[] keyLore = {Msg.LORE_KEY_1, Msg.LORE_KEY_2, Msg.LORE_KEY_3, Msg.LORE_KEY_4};
            for (Msg msg : keyLore)
                if (msg.toString().length() > 0)
                    lore.add(Util.color(msg.toString()));

            WAYGATE_KEY = Util.setItemNameAndLore(new ItemStack(Material.FEATHER, 1), Msg.LORE_KEY_NAME.toString(), lore);
            ItemMeta keyMeta = WAYGATE_KEY.getItemMeta();
            if (keyMeta != null) {
                keyMeta.addEnchant(glow, 1, true);
                WAYGATE_KEY.setItemMeta(keyMeta);
            }

            sr = new ShapedRecipe(new NamespacedKey(pm, "waygatekey"), WAYGATE_KEY);
            sr.shape("RRR", "RKR", "RRR").setIngredient('R', Material.REDSTONE).setIngredient('K', Material.FEATHER);
            Bukkit.addRecipe(sr);
        }
    }

    public void reload() {
        pm.reloadConfig();
        loadConfig(true);
    }

    public String getMsg(Msg msg) {
        return messages.get(msg);
    }

    public Map<Material, Integer> getDefaultBlocksRequired() {
        Material emerald = Material.EMERALD_BLOCK;
        return Stream.of(new Object[][] {
                {emerald, 2}
        }).collect(Collectors.toMap(data -> (Material) data[0], data -> (Integer) data[1]));
    }

    public List<Map<Material, Integer>> getBlocksRequired() {
        ArrayList<Map<Material, Integer>> requiredBlocks = new ArrayList<>();

        for (Map<String, Integer> group : BLOCKS_REQUIRED) {
            Map<Material,Integer> groupedBlocks = new HashMap<>();
            for (Map.Entry requirement : group.entrySet()) {
                try {
                    Material block = Material.getMaterial((String) requirement.getKey());
                    if (block != null) {
                        groupedBlocks.put(block, (Integer) requirement.getValue());
                    }
                } catch (ClassCastException e) {
                    pm.getLogger().warning("Unable to recognise blocks required!");
                }
            }
            requiredBlocks.add(groupedBlocks);
        }

        if (requiredBlocks.size() == 0) {
            pm.getLogger().warning("Using default blocks for Waygate Construction");
            ArrayList<Map<Material, Integer>> defaultBlocks = new ArrayList<>();
            defaultBlocks.add(getDefaultBlocksRequired());
            return defaultBlocks;
        }
        return requiredBlocks;
    }

    public void registerGlow() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            NamespacedKey key = new NamespacedKey(pm, "waygateglow");
            Glow glow = new Glow(key);
            Enchantment.registerEnchantment(glow);
        }
        catch (IllegalArgumentException ignored){}
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void loadWaygates() {
        // Load Networks
        Map<String, Network> networkMap = new HashMap<>();
        if (networksFolder.exists()) {
            File[] _networkFiles = networksFolder.listFiles(new JSONFilenameFilter());
            if (_networkFiles != null) {
                ArrayList<File> networks = new ArrayList<>(Arrays.asList(_networkFiles));
                for (File networkFile : networks) {
                    try {
                        Network network = Network.fromJson(loadData(networkFile));
                        network.clearSystemNetworkStatus();
                        networkMap.put(network.getUUID().toString(), network);
                    } catch (Exception e) {
                        pm.getLogger().warning(String.format("Unable to load Network %s, all worlds in this Network will be placed in Void",
                                networkFile.getName()));
                    }
                }
            }
        }

        // Load Gates
        Map<String, Gate> gateMap = new HashMap<>();
        File[] _worldFiles = worldsFolder.listFiles(new WorldFolderFilenameFilter());
        if (_worldFiles != null) {
            ArrayList<File> folders = new ArrayList<>(Arrays.asList(_worldFiles));
            for (File worldFolder : folders) {
                File[] _gateFiles = worldFolder.listFiles(new JSONFilenameFilter());
                if (_gateFiles != null) {
                    ArrayList<File> gates = new ArrayList<>(Arrays.asList(_gateFiles));
                    for (File gateFile : gates) {
                        try {
                            Gate gate = Gate.fromJson(loadData(gateFile));
                            gateMap.put(gate.getUUID().toString(), gate);
                        } catch (Exception e) {
                            pm.getLogger().warning(String.format("Unable to load Gate %s in World %s",
                                    gateFile.getName(), worldFolder.getName()));
                        }
                    }
                }
            }
        }

        // Sort and Save Gates
        for (Gate gate : gateMap.values()) {
            // Set Network
            Network sysNetwork = Network.getSystemNetworkFromSysUUID(gate.getNetworkUuid());
            if (sysNetwork != null)
                gate.setNetwork(sysNetwork);
            else if (networkMap.containsKey(gate.getNetworkUuid()))
                gate.setNetwork(networkMap.get(gate.getNetworkUuid()));
            else
                gate.setNetwork(Network.getVoidNetwork());

            // Set Fixed Destination
            if (gate.getDestinationUuid() != null) {
                gate.setDestination(gateMap.get(gate.getDestinationUuid()));
            }
        }

        // Save
        wm.loadGates(new ArrayList<>(gateMap.values()));

    }

    public boolean saveWaygate(Gate gate, boolean saveNetwork) {
        String world = gate.getExit().getWorldName();
        File worldFolder = new File(worldsFolder, Util.getKey(world));
        if (!worldFolder.exists())
            worldFolder.mkdir();

        if (saveNetwork && !gate.getNetwork().isSystem()) {
            boolean saved = saveNetwork(gate.getNetwork());
            if (!saved)
                return false;
        }

        return saveData(worldFolder, gate.getUUID(), gate.toJson());
    }

    public boolean saveNetwork(Network network) {
        if (network.isSystem())
            return false;
        return saveData(networksFolder, network.getUUID(), network.toJson());
    }

    public boolean deleteWaygate(Gate gate, boolean deleteNetwork) {
        boolean success = true;
        String world = gate.getExit().getWorldName();
        File worldFolder = new File(worldsFolder, Util.getKey(world));
        if (worldFolder.exists()) {
            success = new File(worldFolder, String.format("%s.json", gate.getUUID())).delete();
            try {
                if (worldFolder.list().length == 0) {
                    worldFolder.delete();
                }
            } catch (Exception e) {
                pm.getLogger().warning(String.format("Unable to check if world folder %s is empty", world));
            }
        }
        if (deleteNetwork) {
            success = success && deleteNetwork(gate.getNetwork());
        }
        return success;
    }

    public boolean deleteNetwork(Network network) {
        if (network.isSystem())
            return false;
        return new File(networksFolder, String.format("%s.json", network.getUUID())).delete();
    }

    private boolean saveData(File dataFolder, UUID uuid, String data) {
        if (dataFolder.exists()) {
            File dataFile = new File(dataFolder, String.format("%s.json", uuid.toString()));

            if (!dataFile.exists()) {
                try {
                    dataFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (dataFile.exists()) {
                try {
                    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dataFile), Charsets.UTF_8);

                    try {
                        writer.write(data);
                    } finally {
                        writer.close();
                    }
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private String loadData(File file) throws IOException {
        String content = "";
        content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        return content;
    }

    static class JSONFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".json");
        }

    }

    static class WorldFolderFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            return new File(dir, name).isDirectory();
        }

    }

}
