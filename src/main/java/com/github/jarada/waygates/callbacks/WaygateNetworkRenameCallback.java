package com.github.jarada.waygates.callbacks;

import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.menus.MenuManager;
import org.bukkit.entity.Player;

public class WaygateNetworkRenameCallback extends ChatCallback {

    private String name;

    public WaygateNetworkRenameCallback(Player player, Gate currentWaygate) {
        super(player, currentWaygate);
        Msg.NETWORK_CHANGE_NAME.sendTo(player, DataManager.getManager().WG_NETWORK_NAME_MAX_LENGTH);
    }

    @Override
    public boolean verify(String chat) {
        return chat.length() <= DataManager.getManager().WG_NETWORK_NAME_MAX_LENGTH;
    }

    @Override
    public void success(String chat) {
        this.name = chat;
        getCurrentWaygate().getNetwork().setName(chat);
        Msg.NETWORK_CHANGE_NAME_SUCCESS.sendTo(getPlayer(), name);
    }

    @Override
    public void failure() {
        Msg.MAX_LENGTH_EXCEEDED.sendTo(getPlayer(), DataManager.getManager().WG_NETWORK_NAME_MAX_LENGTH);
    }

    @Override
    public void callback() {
        if (name != null) {
            // Success
            if (isPlayerNearGate())
                new MenuManager(getPlayer(), getCurrentWaygate()).saveUpdateToNetwork().openWaygateNetworkManageMenu();
            else
                new MenuManager(getPlayer(), getCurrentWaygate()).saveUpdateToNetwork();
        } else {
            // Failed
            if (isPlayerNearGate())
                new MenuManager(getPlayer(), getCurrentWaygate()).openWaygateNetworkManageMenu();
        }
    }
}
