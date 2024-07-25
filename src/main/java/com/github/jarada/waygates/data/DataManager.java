package com.github.jarada.waygates.data;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.types.MenuSize;
import com.github.jarada.waygates.util.Util;
import com.google.common.base.Charsets;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"ResultOfMethodCallIgnored", "rawtypes"})
public class DataManager {

    private static final String WORLDS_FOLDER_FILENAME = "worlds";
    private static final String NETWORKS_FOLDER_FILENAME = "networks";

    private static final String WAYGATE_CONSTRUCTOR_KEY = "waygateconstructor";
    private static final String WAYGATE_KEY_KEY = "waygatekey";
    private static final String WAYGATE_CONTROL_KEY = "waygatecontrol";

    private static DataManager          dm;
    private final PluginMain            pm;
    private final WaygateManager        wm;

    private final File                  worldsFolder;
    private final File                  networksFolder;
    private final Map<Msg, String>      messages;

    public ItemStack                    WAYGATE_CONSTRUCTOR;
    public ItemStack                    WAYGATE_KEY;
    public ItemStack                    WAYGATE_CONTROL;

    public int                          MAX_AREA;
    public List<Map<String, Integer>>   BLOCKS_REQUIRED;
    public MenuSize                     MENU_SIZE;
    public int                          WG_NAME_MAX_LENGTH, WG_DESC_MAX_LENGTH;
    public int                          WG_NETWORK_NAME_MAX_LENGTH;
    public int                          WG_CONTROLLER_DISTANCE;
    public int                          WG_GATE_ACTIVATION_TIME;
    public GateActivationParticles      WG_GATE_EFFECT_PARTICLES;
    public int                          WG_GATE_MINIMAL_DISTANCE;
    public boolean                      WG_GATE_ICON_CHANGE_CONSUMES;
    public boolean                      WG_CONTROL_CREATOR_CONSUMES;
    public boolean                      WG_CONSTRUCTOR_CONSUMES;
    public boolean                      WG_KEY_CONSUMES;
    public boolean                      WG_KEY_PERMANENT;
    public boolean                      WG_ZOMBIE_PIGMAN_ALLOWED;
    public boolean                      WG_PRIVATE_GATES_ALLOW_TRAVEL;

    public DataManager() {
        pm = PluginMain.getPluginInstance();
        wm = WaygateManager.getManager();

        messages = new HashMap<>();
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
        WG_DESC_MAX_LENGTH = config.getInt("Waygates.WG_DESC_MAX_LENGTH");
        WG_NETWORK_NAME_MAX_LENGTH = Integer.max(6, config.getInt("Waygates.WG_NETWORK_NAME_MAX_LENGTH"));
        WG_CONTROLLER_DISTANCE = Integer.min(50, Integer.max(1, config.getInt("Waygates.WG_CONTROLLER_DISTANCE")));
        WG_GATE_ACTIVATION_TIME = Integer.min(300, Integer.max(5, config.getInt("Waygates.WG_GATE_ACTIVATION_TIME")));
        WG_GATE_MINIMAL_DISTANCE = Integer.min(50, Integer.max(1, config.getInt("Waygates.WG_GATE_MINIMAL_DISTANCE")));
        WG_GATE_ICON_CHANGE_CONSUMES = config.getBoolean("Waygates.WG_GATE_ICON_CHANGE_CONSUMES");
        WG_CONTROL_CREATOR_CONSUMES = config.getBoolean("Waygates.WG_CONTROL_CREATOR_CONSUMES");
        WG_CONSTRUCTOR_CONSUMES = config.getBoolean("Waygates.WG_CONSTRUCTOR_CONSUMES");
        WG_KEY_CONSUMES = config.getBoolean("Waygates.WG_KEY_CONSUMES");
        WG_KEY_PERMANENT = config.getBoolean("Waygates.WG_KEY_PERMANENT");
        WG_ZOMBIE_PIGMAN_ALLOWED = config.getBoolean("Waygates.WG_ZOMBIE_PIGMAN_ALLOWED");
        WG_PRIVATE_GATES_ALLOW_TRAVEL = config.getBoolean("Waygates.WG_PRIVATE_GATES_ALLOW_TRAVEL");

        config.addDefault("Waygates.WG_GATE_EFFECT_PARTICLES", "normal");
        try {
            WG_GATE_EFFECT_PARTICLES = GateActivationParticles.valueOf(Objects.requireNonNull(config.getString("Waygates.WG_GATE_EFFECT_PARTICLES")).toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            // Using Static Default
            WG_GATE_EFFECT_PARTICLES = GateActivationParticles.NORMAL;
            pm.getLogger().warning("Invalid Effects Particle Size in Config, using normal!");
        }

        try {
            MENU_SIZE = MenuSize.valueOf(config.getString("Waygates.MENU_SIZE").toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            MENU_SIZE = MenuSize.COMPACT;
            pm.getLogger().warning("Invalid Menu Size in Config, using compact!");
        }

        BLOCKS_REQUIRED = new ArrayList<>();
        try {
            for (String group : Objects.requireNonNull(config.getConfigurationSection("Waygates.BLOCKS_REQUIRED")).getKeys(false)) {
                HashMap<String, Integer> requirement = new HashMap<>();
                for (String inv : Objects.requireNonNull(config.getConfigurationSection("Waygates.BLOCKS_REQUIRED." + group)).getKeys(false)) {
                    requirement.put(inv, config.getInt("Waygates.BLOCKS_REQUIRED." + group + "." + inv));
                }
                BLOCKS_REQUIRED.add(requirement);
            }
            if (BLOCKS_REQUIRED.isEmpty())
                throw new NullPointerException();
            config.addDefault("Waygates.BLOCKS_REQUIRED", null);
        } catch (NullPointerException e) {
            // Using Static Default
            pm.getLogger().warning("Using Static Default for Blocks Required");
        }

        config.options().copyDefaults(true);
        pm.saveConfig();

        if (!reload) {
            List<String> lore = new ArrayList<>();
            Msg[] constructorLore = {Msg.LORE_CONSTRUCTOR_1, Msg.LORE_CONSTRUCTOR_2, Msg.LORE_CONSTRUCTOR_3, Msg.LORE_CONSTRUCTOR_4};
            for (Msg msg : constructorLore)
                if (msg.toString().length() > 0)
                    lore.add(Util.color(msg.toString()));


            WAYGATE_CONSTRUCTOR = Util.setItemNameAndLore(
                NBTItemManager.getNBTItem(new ItemStack(Material.GOLD_NUGGET, 1), WAYGATE_CONSTRUCTOR_KEY),
                Msg.LORE_CONSTRUCTOR_NAME.toString(), lore);
            ItemMeta activatorMeta = WAYGATE_CONSTRUCTOR.getItemMeta();
            if (activatorMeta != null) {
                activatorMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                activatorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                WAYGATE_CONSTRUCTOR.setItemMeta(activatorMeta);
            }

            ShapedRecipe sr = new ShapedRecipe(new NamespacedKey(pm, WAYGATE_CONSTRUCTOR_KEY), WAYGATE_CONSTRUCTOR);
            sr.shape("RRR", "RGR", "RRR").setIngredient('R', Material.REDSTONE).setIngredient('G', Material.GOLD_NUGGET);
            boolean recipeResult = Bukkit.addRecipe(sr);
            if (!recipeResult)
                pm.getLogger().warning("Unable to load recipe! Waygate Constructors will be uncraftable");

            lore = new ArrayList<>();
            Msg[] keyLore = {Msg.LORE_KEY_1, Msg.LORE_KEY_2, Msg.LORE_KEY_3, Msg.LORE_KEY_4};
            for (Msg msg : keyLore)
                if (msg.toString().length() > 0)
                    lore.add(Util.color(msg.toString()));

            WAYGATE_KEY = Util.setItemNameAndLore(
                    NBTItemManager.getNBTItem(new ItemStack(Material.FEATHER, 1), WAYGATE_KEY_KEY),
                    Msg.LORE_KEY_NAME.toString(), lore);
            ItemMeta keyMeta = WAYGATE_KEY.getItemMeta();
            if (keyMeta != null) {
                keyMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                keyMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                WAYGATE_KEY.setItemMeta(keyMeta);
            }

            sr = new ShapedRecipe(new NamespacedKey(pm, WAYGATE_KEY_KEY), WAYGATE_KEY);
            sr.shape("RRR", "RKR", "RRR").setIngredient('R', Material.REDSTONE).setIngredient('K', Material.FEATHER);
            recipeResult = Bukkit.addRecipe(sr);
            if (!recipeResult)
                pm.getLogger().warning("Unable to load recipe! Waygate Keys will be uncraftable");

            lore = new ArrayList<>();
            Msg[] controlLore = {Msg.LORE_CONTROL_1, Msg.LORE_CONTROL_2, Msg.LORE_CONTROL_3, Msg.LORE_CONTROL_4};
            for (Msg msg : controlLore)
                if (msg.toString().length() > 0)
                    lore.add(Util.color(msg.toString()));

            WAYGATE_CONTROL = Util.setItemNameAndLore(
                    NBTItemManager.getNBTItem(new ItemStack(Material.IRON_NUGGET, 1), WAYGATE_CONTROL_KEY),
                    Msg.LORE_CONTROL_NAME.toString(), lore);
            ItemMeta controlMeta = WAYGATE_CONTROL.getItemMeta();
            if (controlMeta != null) {
                controlMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                controlMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                WAYGATE_CONTROL.setItemMeta(controlMeta);
            }

            sr = new ShapedRecipe(new NamespacedKey(pm, WAYGATE_CONTROL_KEY), WAYGATE_CONTROL);
            sr.shape("CKC")
                    .setIngredient('C', new RecipeChoice.ExactChoice(WAYGATE_CONSTRUCTOR))
                    .setIngredient('K', new RecipeChoice.ExactChoice(WAYGATE_KEY));
            recipeResult = Bukkit.addRecipe(sr);
            if (!recipeResult)
                pm.getLogger().warning("Unable to load recipe! Waygate Controllers will be uncraftable");
        }
    }

    public void reload() {
        pm.reloadConfig();
        loadConfig(true);
    }

    public String getMsg(Msg msg) {
        return messages.get(msg);
    }

    public ItemStack getLockForGate(Gate gate) {
        List<String> lore = getLockLoreForGate(gate);
        ItemStack lock = Util.setItemNameAndLore(
                NBTItemManager.getLockNBTItem(new ItemStack(Material.FEATHER, 1), WAYGATE_KEY_KEY,
                        gate.getUUID().toString()),
                Msg.LORE_KEY_LOCK_NAME.toString(gate.getName()), lore);
        ItemMeta keyMeta = lock.getItemMeta();
        if (keyMeta != null) {
            keyMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            keyMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            lock.setItemMeta(keyMeta);
        }

        return lock;
    }

    public List<String> getLockLoreForGate(Gate gate) {
        ArrayList<String> lore = new ArrayList<>();
        Msg[] keyLore = {Msg.LORE_KEY_LOCK_1, Msg.LORE_KEY_LOCK_2,
                Msg.LORE_KEY_LOCK_3, Msg.LORE_KEY_LOCK_4};
        for (Msg msg : keyLore) {
            if (msg.toString().length() > 0) {
                lore.add(Util.color(msg.toString()));
            }
        }
        if (!NBTItemManager.isNBTEnabled())
            lore.add(Util.color(Util.getGateUUIDLore(gate.getUUID().toString())));
        return lore;
    }

    public boolean isLockKeyValid(Gate gate, ItemStack is) {
        ItemMeta im = is.getItemMeta();
        if (im == null)
            return false;

        List<String> keyLore = getLockLoreForGate(gate);
        boolean loreValid = im.getDisplayName().equals(Util.color(Msg.LORE_KEY_LOCK_NAME.toString(gate.getName()))) &&
                im.getLore() != null && im.getLore().size() == keyLore.size() &&
                im.getLore().stream().allMatch(x -> {
                    if (keyLore.contains(x)) {
                        keyLore.remove(x);
                        return true;
                    }
                    return false;
                });
        return keyLore.isEmpty() && loreValid && NBTItemManager.isLockNBTItem(is, gate.getUUID().toString());
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

        if (requiredBlocks.isEmpty()) {
            pm.getLogger().warning("Using default blocks for Waygate Construction");
            ArrayList<Map<Material, Integer>> defaultBlocks = new ArrayList<>();
            defaultBlocks.add(getDefaultBlocksRequired());
            return defaultBlocks;
        }
        return requiredBlocks;
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

        // Load Gates and Controllers
        List<Controller> controllers = new ArrayList<>();
        Map<String, Gate> gateMap = new HashMap<>();
        File[] _worldFiles = worldsFolder.listFiles(new WorldFolderFilenameFilter());
        if (_worldFiles != null) {
            ArrayList<File> folders = new ArrayList<>(Arrays.asList(_worldFiles));
            for (File worldFolder : folders) {
                File[] _gateFiles = worldFolder.listFiles(new JSONFilenameFilter());
                if (_gateFiles != null) {
                    ArrayList<File> gates = new ArrayList<>(Arrays.asList(_gateFiles));
                    for (File gateFile : gates) {
                        if (gateFile.getName().startsWith("c-")) {
                            try {
                                Controller controller = Controller.fromJson(loadData(gateFile));
                                controllers.add(controller);
                            } catch (Exception e) {
                                pm.getLogger().warning(String.format("Unable to load Controller %s in World %s",
                                        gateFile.getName(), worldFolder.getName()));
                            }
                        } else {
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
                gate.setFixedDestination(gateMap.get(gate.getDestinationUuid()));
            }

            // Set Active Destination
            if (gate.getActiveDestinationUuid() != null) {
                gate.setActiveDestination(gateMap.get(gate.getActiveDestinationUuid()));
            }
        }
        controllers.forEach(controller -> {
            if (controller.getGateUuid() != null) {
                controller.setGate(gateMap.get(controller.getGateUuid()));
            }
        });

        // Save
        wm.loadGates(new ArrayList<>(gateMap.values()));
        wm.loadControllers(controllers);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean saveController(Controller controller) {
        String world = controller.getWorldName();
        File worldFolder = new File(worldsFolder, Util.getKey(world));
        if (!worldFolder.exists())
            worldFolder.mkdir();

        return saveData(worldFolder, controller.getUUID(), controller.toJson(), "c-");
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean saveWaygate(Gate gate, boolean saveNetwork) {
        String world = gate.getWorldName();
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

    @SuppressWarnings("UnusedReturnValue")
    public boolean deleteController(Controller controller) {
        boolean success = true;
        String world = controller.getWorldName();
        File worldFolder = new File(worldsFolder, Util.getKey(world));
        if (worldFolder.exists()) {
            success = new File(worldFolder, String.format("c-%s.json", controller.getUUID())).delete();
            try {
                if (Objects.requireNonNull(worldFolder.list()).length == 0) {
                    worldFolder.delete();
                }
            } catch (Exception e) {
                pm.getLogger().warning(String.format("Unable to check if world folder %s is empty", world));
            }
        }
        return success;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean deleteWaygate(Gate gate, boolean deleteNetwork) {
        boolean success = true;
        String world = gate.getWorldName();
        File worldFolder = new File(worldsFolder, Util.getKey(world));
        if (worldFolder.exists()) {
            success = new File(worldFolder, String.format("%s.json", gate.getUUID())).delete();
            try {
                if (Objects.requireNonNull(worldFolder.list()).length == 0) {
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
        return saveData(dataFolder, uuid, data, "");
    }

    private boolean saveData(File dataFolder, UUID uuid, String data, String prefix) {
        if (dataFolder.exists()) {
            File dataFile = new File(dataFolder, String.format("%s%s.json", prefix, uuid.toString()));

            if (!dataFile.exists()) {
                try {
                    dataFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (dataFile.exists()) {
                try {

                    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dataFile), Charsets.UTF_8)) {
                        writer.write(data);
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
        return new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
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

    static class NBTItemManager {

        public static boolean isNBTEnabled() {
            return Bukkit.getPluginManager().isPluginEnabled("NBTAPI");
        }

        public static ItemStack getNBTItem(ItemStack item, String key) {
            if (isNBTEnabled()) {
                NBTItem nbtItem = new NBTItem(item);
                nbtItem.setByte("DoesNotConvert", (byte) 1);
                nbtItem.setByte(key, (byte) 1);
                return nbtItem.getItem();
            }
            return item;
        }

        public static ItemStack getLockNBTItem(ItemStack item, String key, String uuid) {
            if (isNBTEnabled()) {
                NBTItem nbtItem = new NBTItem(getNBTItem(item, key));
                nbtItem.setByte(String.format("WG-%s", uuid), (byte) 1);
                return nbtItem.getItem();
            }
            return item;
        }

        public static boolean isLockNBTItem(ItemStack item, String uuid) {
            if (isNBTEnabled()) {
                return new NBTItem(item).hasKey(String.format("WG-%s", uuid));
            }
            return true;
        }

    }

}
