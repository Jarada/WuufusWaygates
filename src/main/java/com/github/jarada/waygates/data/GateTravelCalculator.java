package com.github.jarada.waygates.data;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class GateTravelCalculator {

    public static double distanceForGate(BlockLocation gate, BlockLocation destination) {
        double distance;
        if (gate.getWorldName().equals(destination.getWorldName())) {
            distance = gate.distance(destination);
        } else {
            BlockLocation fromLead = new BlockLocation(gate.getWorldName(), 0, 0, 0);
            BlockLocation toLead = new BlockLocation(destination.getWorldName(), 0, 0, 0);
            distance = gate.distance(fromLead) + destination.distance(toLead);
        }
        return distance;
    }

    public static int costForDistance(BlockLocation gate, BlockLocation destination) {
        return (int) Math.floor(distanceForGate(gate, destination) / 500) + 1;
    }

    public static int diamondsOnPlayer(Player p, boolean blocks) {
        HashMap<Integer, ? extends ItemStack> diamonds = p.getInventory().all(
                blocks ? Material.DIAMOND_BLOCK : Material.DIAMOND);

        int total = 0;
        for (ItemStack diamondStack : diamonds.values()) {
            total += diamondStack.getAmount();
        }

        return total;
    }

    public static int resourceOnPlayer(Player p) {
        return diamondsOnPlayer(p, false) + (diamondsOnPlayer(p, true) * 9);
    }

    public static void removeDiamondsForCost(Player p, int cost) {
        int blocks = Math.min(cost / 9, diamondsOnPlayer(p, true));
        int diamonds = (blocks > 0) ? cost - (blocks * 9) : cost;
        if (blocks > 0)
            p.getInventory().removeItem(new ItemStack(Material.DIAMOND_BLOCK, blocks));
        if (diamonds > 0)
            p.getInventory().removeItem(new ItemStack(Material.DIAMOND, diamonds));
    }

}
