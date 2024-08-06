package com.github.jarada.waygates.callbacks;

import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.menus.MenuManager;
import org.bukkit.entity.Player;

public class WaygateNetworkCreateCallback extends ChatCallback {

    private String name;

    public WaygateNetworkCreateCallback(Player player, Gate currentWaygate) {
        super(player, currentWaygate);
        Msg.NETWORK_CREATE_SET_NAME.sendTo(player, DataManager.getManager().WG_NETWORK_NAME_MAX_LENGTH);
    }

    @Override
    public boolean verify(String chat) {
        return chat.length() <= DataManager.getManager().WG_NETWORK_NAME_MAX_LENGTH;
    }

    @Override
    public void success(String chat) {
        this.name = chat;
        Msg.NETWORK_CREATE_SET_TYPE.sendTo(getPlayer());
    }

    @Override
    public void failure() {
        Msg.MAX_LENGTH_EXCEEDED.sendTo(getPlayer(), DataManager.getManager().WG_NETWORK_NAME_MAX_LENGTH);
    }

    @Override
    public void callback() {
        if (isPlayerNearGate())
            new MenuManager(getPlayer(), getCurrentWaygate()).openWaygateNetworkTypeMenu(name);
    }

    @Override
    public void expire() {
        // Do Nothing
    }
}
