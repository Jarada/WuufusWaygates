package com.github.jarada.waygates.callbacks;

import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.data.Network;
import com.github.jarada.waygates.menus.MenuManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class WaygateNetworkIconChangeCallback extends IconCallback {

    private Material icon;

    public WaygateNetworkIconChangeCallback(Player player, Gate currentWaygate) {
        super(player, currentWaygate);
        Msg.NETWORK_CHANGE_ICON.sendTo(player, currentWaygate.getNetwork().getName());
    }

    @Override
    public boolean verify(Material icon) {
        return Network.systemIcons().stream()
                .noneMatch(x -> x.equals(icon));
    }

    @Override
    public void success(Material icon) {
        this.icon = icon;
        getCurrentWaygate().getNetwork().setIcon(icon);
        Msg.NETWORK_CHANGE_ICON_SUCCESS.sendTo(getPlayer());
    }

    @Override
    public void failure() {
        Msg.NETWORK_CHANGE_ICON_FAILURE.sendTo(getPlayer());
    }

    @Override
    public void callback() {
        if (icon != null) {
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
