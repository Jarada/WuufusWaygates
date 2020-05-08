package com.github.jarada.waygates.callbacks;

import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.menus.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WaygateOwnerChangeCallback extends ChatCallback {

    private Player ownerChange;

    public WaygateOwnerChangeCallback(Player player, Gate currentWaygate) {
        super(player, currentWaygate);
        Msg.GATE_CHANGE_OWNER.sendTo(player);
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
        getCurrentWaygate().setOwner(ownerChange.getUniqueId());
        Msg.GATE_CHANGE_OWNER_SUCCESS.sendTo(getPlayer(), getCurrentWaygate().getName(), ownerChange.getName());
    }

    @Override
    public void failure() {
        Msg.GATE_CHANGE_OWNER_FAILED.sendTo(getPlayer());
    }

    @Override
    public void callback() {
        if (ownerChange != null) {
            // Success
            if (getCurrentWaygate().isOwnerPrivate() && !getPlayer().hasPermission("wg.bypass")) {
                new MenuManager(getPlayer(), getCurrentWaygate()).saveUpdateToGate();
                return;
            }

            new MenuManager(getPlayer(), getCurrentWaygate()).saveUpdateToGate().openWaygateMenu();
        } else {
            // Failed
            new MenuManager(getPlayer(), getCurrentWaygate()).openWaygateSettingsMenu();
        }
    }
}
