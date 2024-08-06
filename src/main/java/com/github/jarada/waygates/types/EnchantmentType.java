package com.github.jarada.waygates.types;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

public enum EnchantmentType {

    LUCK {
        @Override
        public Enchantment get() {
            try {
                // 1.20- Support
                Enchantment.class.getDeclaredField("LUCK");
                NamespacedKey namespacedKey = NamespacedKey.minecraft("luck_of_the_sea");
                return Enchantment.getByKey(namespacedKey);
            } catch (NoSuchFieldException e) {
                return Enchantment.LUCK_OF_THE_SEA;
            }
        }
    };

    public abstract Enchantment get();
}
