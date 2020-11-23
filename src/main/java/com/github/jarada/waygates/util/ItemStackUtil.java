package com.github.jarada.waygates.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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

            if (o1.getType() == Material.PLAYER_HEAD && o2.getType() == Material.PLAYER_HEAD) {
                // We verify the inventory on other items, player heads just cause problems
                // So providing the two items are player heads that will do for us
                continue;
            }

            if (!(Objects.equals(o1, o2)))
                return false;
        }

        return true;
    }

}
