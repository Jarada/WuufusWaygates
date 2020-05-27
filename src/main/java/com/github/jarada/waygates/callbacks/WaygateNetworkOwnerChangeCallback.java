package com.github.jarada.waygates.callbacks;

import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.menus.MenuManager;
import com.github.jarada.waygates.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WaygateNetworkOwnerChangeCallback extends ChatCallback {

    private Player ownerChange;

    public WaygateNetworkOwnerChangeCallback(Player player, Gate currentWaygate) {
        super(player, currentWaygate);
        Msg.NETWORK_CHANGE_OWNER.sendTo(player, currentWaygate.getNetwork().getName());
    }

    @Override
    public boolean verify(String chat) {
        if (chat != null && chat.length() > 0) {
            Player owner = Bukkit.getPlayer(chat);
            if (owner != null) {
                ownerChange = owner;
                return true;
            }
        }
        return false;
    }

    @Override
    public void success(String chat) {
        getCurrentWaygate().getNetwork().setOwner(ownerChange.getUniqueId());
        Msg.NETWORK_CHANGE_OWNER_SUCCESS.sendTo(getPlayer(), getCurrentWaygate().getNetwork().getName(),
                ownerChange.getName());
    }

    @Override
    public void failure() {
        Msg.NETWORK_CHANGE_OWNER_FAILED.sendTo(getPlayer());
    }

    @Override
    public void callback() {
        if (ownerChange != null) {
            // Success
            if (!getPlayer().hasPermission("wg.admin") || !isPlayerNearGate()) {
                new MenuManager(getPlayer(), getCurrentWaygate()).saveUpdateToNetwork();
                return;
            }
            
            new MenuManager(getPlayer(), getCurrentWaygate()).saveUpdateToNetwork().openWaygateNetworkManageMenu();
        } else {
            // Failed
            if (!isPlayerNearGate())
                new MenuManager(getPlayer(), getCurrentWaygate()).openWaygateNetworkMenu();
        }
    }
}
