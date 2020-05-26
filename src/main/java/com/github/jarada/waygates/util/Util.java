package com.github.jarada.waygates.util;

import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.Metadatable;

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
        im.setDisplayName(color(name));

        if (lore != null)
            im.setLore(lore);

        item.setItemMeta(im);
        return item;
    }

    public static String[] getWrappedLore(String description, int maxLineLength) {
        return WordUtils.wrap(description, maxLineLength, "\n", true).split("\\n");
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
        if (isNpc(object)) return false;
        return true;
    }

    public static ItemStack getHead(OfflinePlayer player, String name, List<String> lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setOwningPlayer(player);
        meta.setDisplayName(color(name));
        if (lore != null)
            meta.setLore(lore);

        item.setItemMeta(meta);
        return item;
    }

    /* Location */

    public static void checkChunkLoad(final Block b) {
        final World w = b.getWorld();
        final Chunk c = b.getChunk();

        if (!w.isChunkLoaded(c)) {
            w.loadChunk(c);
        }
    }

    public static void playEffect(Location loc, Effect effect) {
        loc.getWorld().playEffect(loc, effect, 0);
    }

    public static void playParticle(Location loc, Particle particle, int count) {
        if (particle == Particle.REDSTONE) {
            Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1);
            loc.getWorld().spawnParticle(particle, loc, count, 0, 0, 0, 0, dustOptions);
        } else {
            loc.getWorld().spawnParticle(particle, loc, count);
        }
    }

    public static void playSound(Location loc, Sound sound) {
        loc.getWorld().playSound(loc, sound, 10F, 1F);
    }
    
    /* Key */

    public static String getKey(String string) {
        return stripColor(string.toLowerCase()).replaceAll(" ", "_");
    }

}
