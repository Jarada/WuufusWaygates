package com.github.jarada.waygates.callbacks;

import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.menus.MenuManager;
import com.github.jarada.waygates.util.Util;
import org.bukkit.entity.Player;

public class WaygateRenameCallback extends ChatCallback {

    private final String oldName;

    public WaygateRenameCallback(Player player, Gate currentWaygate) {
        super(player, currentWaygate);
        this.oldName = currentWaygate.getName();
        Msg.GATE_SET_NAME.sendTo(player, DataManager.getManager().WG_NAME_MAX_LENGTH);
    }

    @Override
    public boolean verify(String chat) {
        return chat.length() <= DataManager.getManager().WG_NAME_MAX_LENGTH;
    }

    @Override
    public void success(String chat) {
        getCurrentWaygate().setName(chat);
        Msg.RENAMED.sendTo(getPlayer(), oldName, getCurrentWaygate().getName());
    }

    @Override
    public void failure() {
        Msg.MAX_LENGTH_EXCEEDED.sendTo(getPlayer(), DataManager.getManager().WG_NAME_MAX_LENGTH);
    }

    @Override
    public void callback() {
        if (isPlayerNearGate())
            new MenuManager(getPlayer(), getCurrentWaygate()).saveUpdateToGate().openWaygateSettingsMenu();
        else
            new MenuManager(getPlayer(), getCurrentWaygate()).saveUpdateToGate();
    }

    @Override
    public void expire() {
        new MenuManager(getPlayer(), getCurrentWaygate()).saveUpdateToGate();
    }
}
