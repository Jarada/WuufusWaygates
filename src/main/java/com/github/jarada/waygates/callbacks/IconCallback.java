package com.github.jarada.waygates.callbacks;

import com.github.jarada.waygates.data.Gate;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public abstract class IconCallback extends Callback<Material> {

    public IconCallback(Player player, Gate currentWaygate) {
        super(player, currentWaygate);
    }

    public abstract boolean verify(Material icon);

    public abstract void success(Material icon);

}
