package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.callbacks.WaygateNetworkCreateCallback;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.data.Network;
import com.github.jarada.waygates.listeners.ChatListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class WaygateNetworkMenu extends Menu {

    List<Network>       networkList;
    private Network[]   optionNetworks;

    public WaygateNetworkMenu(MenuManager mm, Player p, Gate currentWaygate, List<Network> networkList) {
        super(mm, p, currentWaygate);
        this.networkList = networkList;
        setup();
    }

    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        if (optionNetworks[slot] != null) {
            Network network = optionNetworks[slot];
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    WaygateManager.getManager().changeGateNetwork(currentWaygate, network, false);
                    mm.openWaygateSettingsMenu();
                }

            });
        }

        if (optionNames[slot].equals("Close")) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    mm.openWaygateSettingsMenu();
                }

            });
        } else if (optionNames[slot].equals("Create")) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    p.closeInventory();
                    new ChatListener(new WaygateNetworkCreateCallback(p, currentWaygate));
                }

            });
        } else {
            super.onInventoryClick(clickEvent);
        }
    }

    @Override
    public void buildMenu() {
        initMenu();
        optionNetworks = new Network[size];

        for (int slot = 0; slot < 9; slot++) {
            int index = ((page - 1) * 9) + slot;

            if (index > networkList.size() - 1)
                break;

            Network nw = networkList.get(index);
            addNetworkToMenu(slot, nw);
        }

        if (page > 1) {
            addPreviousToMenu();
        }

        if (networkList.size() > 9) {
            addPageToMenu();
        }

        if (networkList.size() > page * 9) {
            addNextToMenu();
        }

        if (p.hasPermission("wg.create.network"))
            addAddNetworkToMenu();

        if (!currentWaygate.getNetwork().isSystem() && (currentWaygate.getNetwork().getOwner().equals(p.getUniqueId()) || p.hasPermission("wg.admin"))) {
            addItemToMenu(16, Material.LEVER, Msg.MENU_TITLE_NETWORK_MANAGE.toString(), "Manage");
        }

        addCloseToMenu();
    }

    void addNetworkToMenu(int slot, Network nw) {
        super.addNetworkToMenu(slot, nw, true);
        optionNetworks[slot] = nw;
    }

    void addAddNetworkToMenu() {
        addItemToMenu(9, Material.WRITABLE_BOOK, Msg.MENU_TITLE_NETWORK_CREATE.toString(), "Create");
    }
}
