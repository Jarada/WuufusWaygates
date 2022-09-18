package com.github.jarada.waygates.util;

import org.apache.commons.text.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.Metadatable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Util {

    /* Color */

    public static String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String stripColor(String string) {
        return ChatColor.stripColor(color(string));
    }

    /* Items */

    public static ItemStack setItemNameAndLore(ItemStack item, String name, List<String> lore) {
        ItemMeta im = item.getItemMeta();
        if (im != null) {
            im.setDisplayName(color(name));

            if (lore != null)
                im.setLore(lore);

            item.setItemMeta(im);
        }
        return item;
    }

    public static String[] getWrappedLore(String description, int maxLineLength) {
        return WordUtils.wrap(description, maxLineLength, "\n", true).split("\\n");
    }

    public static String getGateUUIDLore(String uuid) {
        return String.format("&0%s", uuid);
    }

    /* Materials */

    @SuppressWarnings("deprecation")
    public static boolean isMaterialAir(Material material) {
        Class<Material> materialClass = Material.class;
        try {
            materialClass.getMethod("isAir", (Class<?>) null);
            return material.isAir();
        } catch (NoSuchMethodException e) {
            // 1.13 support
            return Arrays.asList(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR, Material.LEGACY_AIR).contains(material);
        }
    }

    /* Player */

    public static boolean isNpc(Object object) {
        if (!(object instanceof Metadatable)) return false;
        Metadatable metadatable = (Metadatable)object;
        try {
            return metadatable.hasMetadata("NPC");
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    public static boolean isPlayer(Object object) {
        if (!(object instanceof Player)) return false;
        return !isNpc(object);
    }

    public static ItemStack getHead(OfflinePlayer player, String name, List<String> lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            meta.setDisplayName(color(name));
            if (lore != null)
                meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /* Player Location */

    public static boolean isPlayerNearby(Player player, Location loc, int distance) {
        int d2 = distance * distance;
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.equals(player) && p.getWorld() == loc.getWorld() && p.getLocation().distanceSquared(loc) <= d2) {
                return true;
            }
        }
        return false;
    }

    public static List<Player> getNearbyPlayers(Location loc, int distance) {
        List<Player> res = new ArrayList<>();
        int d2 = distance * distance;
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getWorld() == loc.getWorld() && p.getLocation().distanceSquared(loc) <= d2) {
                res.add(p);
            }
        }
        return res;
    }

    /* Location */

    public static void checkChunkLoad(final Block b) {
        final World w = b.getWorld();
        final Chunk c = b.getChunk();

        if (!w.isChunkLoaded(c)) {
            w.getChunkAt(b).load();
        }
    }

    public static void playEffect(Location loc, Effect effect) {
        World world = loc.getWorld();
        if (world != null)
            world.playEffect(loc, effect, 0);
    }

    public static void playParticle(Location loc, Particle particle, int count) {
        World world = loc.getWorld();
        if (world != null) {
            if (particle == Particle.REDSTONE) {
                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1);
                world.spawnParticle(particle, loc, count, 0, 0, 0, 0, dustOptions);
            } else {
                world.spawnParticle(particle, loc, count);
            }
        }
    }

    public static void playSound(Location loc, Sound sound) {
        World world = loc.getWorld();
        if (world != null)
            world.playSound(loc, sound, 1.5F, 1F);
    }
    
    /* Key */

    public static String getKey(String string) {
        return stripColor(string.toLowerCase()).replaceAll(" ", "_");
    }

}
