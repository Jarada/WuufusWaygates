package com.github.jarada.waygates.callbacks;

import com.github.jarada.waygates.data.Gate;
import org.bukkit.entity.Player;

public abstract class ChatCallback extends Callback<String> {

    public ChatCallback(Player player, Gate currentWaygate) {
        super(player, currentWaygate);
    }

    public abstract boolean verify(String chat);

    public abstract void success(String chat);

}
