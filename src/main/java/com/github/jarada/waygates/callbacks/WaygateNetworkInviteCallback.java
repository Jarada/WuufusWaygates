package com.github.jarada.waygates.callbacks;

import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.menus.MenuManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WaygateNetworkInviteCallback extends ChatCallback {

    Player invite;
    boolean failedExists;

    public WaygateNetworkInviteCallback(Player player, Gate currentWaygate) {
        super(player, currentWaygate);
        Msg.NETWORK_CHANGE_INVITE_ADD.sendTo(player, currentWaygate.getNetwork().getName());
    }

    @Override
    public boolean verify(String chat) {
        if (chat != null && chat.length() > 0) {
            Player add = Bukkit.getPlayer(chat);
            if (add != null) {
                invite = add;
                if (getCurrentWaygate().getNetwork().isInvitedUser(add.getUniqueId())) {
                    failedExists = true;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void success(String chat) {
        getCurrentWaygate().getNetwork().addInvitedUser(invite.getUniqueId());
        Msg.NETWORK_CHANGE_INVITE_SUCCESS.sendTo(getPlayer(), invite.getName(),
                getCurrentWaygate().getNetwork().getName());
    }

    @Override
    public void failure() {
        if (failedExists) {
            Msg.NETWORK_CHANGE_INVITE_DUPE.sendTo(getPlayer(), invite.getName(),
                    getCurrentWaygate().getNetwork().getName());
        } else {
            Msg.NETWORK_CHANGE_INVITE_FAILED.sendTo(getPlayer());
        }
    }

    @Override
    public void callback() {
        if (invite != null && !failedExists)
            new MenuManager(getPlayer(), getCurrentWaygate()).saveUpdateToNetwork().openWaygateNetworkManageMenu();
        else
            new MenuManager(getPlayer(), getCurrentWaygate()).openWaygateNetworkManageMenu();
    }
}
