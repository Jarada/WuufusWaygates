package com.github.jarada.waygates.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ItemStackUtil {

    /**
     * Created to replace Arrays.equals() to deal with PlayerHead equality inside
     * the Waygate plugin menus (as the PlayerHeads internals differ even if
     * everything else remains equals!)
     *
     * @param a ItemStack array
     * @param a2 ItemStack array
     * @return if ItemStack arrays are equal
     */
    public static boolean equals(@Nullable ItemStack[] a, @Nullable ItemStack[] a2){
        if (a==a2)
            return true;

        if (a==null || a2==null)
            return false;

        int length = a.length;
        if (a2.length != length)
            return false;

        for (int i=0; i<length; i++) {
            ItemStack o1 = a[i];
            ItemStack o2 = a2[i];

            if (o1 == null && o2 == null)
                continue;

            if (o1 == null || o2 == null)
                return false;

            if (o1.getType() != o2.getType())
                return false;

            if (o1.getType() == Material.PLAYER_HEAD) {
                // We verify the inventory on other items, player heads just cause problems
                // So providing the two items are player heads that will do for us
                continue;
            }

            if (o1.getItemMeta() instanceof BlockStateMeta && o2.getItemMeta() instanceof BlockStateMeta) {
                // Block state meta spontaneously creates "internal" data that causes it to not be equal.
                // So, we create placeholders for the state part of the meta and compare them.
                BlockState state = Bukkit.createBlockData(o1.getType()).createBlockState();
                BlockStateMeta m1 = (BlockStateMeta) o1.getItemMeta();
                BlockStateMeta m2 = (BlockStateMeta) o2.getItemMeta();
                m1.setBlockState(state);
                m2.setBlockState(state);
                return m1.equals(m2);
            }

            if (!(Objects.equals(o1, o2)))
                return false;
        }

        return true;
    }

}
