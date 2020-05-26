package com.github.jarada.waygates.data;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings( "deprecation" )
public class Glow extends Enchantment {

    public Glow(NamespacedKey i) {
        super(i);
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack arg0) {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment arg0) {
        return false;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ALL;
    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @NotNull
    @Override
    public String getName() {
        return "Waygate Energy";
    }

    @Override
    public int getStartLevel() {
        return 0;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

}
