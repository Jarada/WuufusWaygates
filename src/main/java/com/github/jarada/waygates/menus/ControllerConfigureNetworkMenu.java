package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.data.Controller;
import com.github.jarada.waygates.data.Network;
import com.github.jarada.waygates.types.MenuSize;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class ControllerConfigureNetworkMenu extends Menu {

    List<Network>       networkList;
    private Network[]   optionNetworks;

    public ControllerConfigureNetworkMenu(MenuManager mm, Player p, Controller currentController, List<Network> networkList) {
        super(mm, p, currentController, false);
        this.networkList = networkList;
        setup();
    }

    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        if (optionNetworks[slot] != null) {
            Network network = optionNetworks[slot];
            Bukkit.getScheduler().runTask(pm, () -> mm.openControllerConfigureGateMenu(network));
        } else if (optionNames[slot].equals("Close")) {
            Bukkit.getScheduler().runTask(pm, () -> {
                mm.close();
            });
        } else {
            super.onInventoryClick(clickEvent);
        }
    }

    void addNetworkToMenu(int slot, Network nw) {
        super.addNetworkToMenu(slot, nw, true);
        optionNetworks[slot] = nw;
    }

    @Override
    public void buildMenu() {
        initMenu();
        optionNetworks = new Network[size];

        for (int slot = 0; slot < MenuSize.STEP_SIZE; slot++) {
            int index = ((page - 1) * MenuSize.STEP_SIZE) + slot;

            if (index > networkList.size() - 1)
                break;

            Network nw = networkList.get(index);
            addNetworkToMenu(slot, nw);
        }

        if (page > 1) {
            addPreviousToMenu();
        }

        if (networkList.size() > getPageSize()) {
            addPageToMenu();
        }

        if (networkList.size() > page * getPageSize()) {
            addNextToMenu();
        }

        addControllerOwnerToMenu();
        addCloseToMenu();
    }

    @Override
    public int getDesiredMenuSize() {
        return MenuSize.getAppropriateMenuSize(networkList.size());
    }

}
