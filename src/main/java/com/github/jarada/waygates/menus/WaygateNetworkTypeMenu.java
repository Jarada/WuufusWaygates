package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.data.Network;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class WaygateNetworkTypeMenu extends Menu {

    private String name;

    WaygateNetworkTypeMenu(MenuManager mm, Player p, Gate currentWaygate, String name) {
        super(mm, p, currentWaygate);
        this.name = name;
        setup();
    }

    @Override
    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        if (optionNames[slot].equals("Close")) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    mm.openWaygateNetworkMenu();
                }

            });
        } else if (optionNames[slot].matches("Private|Invite|Global")) {
            // We have one of the network types
            int type = 0;  // Global 0, Private 1, Invite 2
            if (optionNames[slot].equals("Private"))
                type = 1;
            else if (optionNames[slot].equals("Invite"))
                type = 2;

            if (mm.isNetworkNameUnique(name)) {
                Network network = new Network(name, type == 1, type == 2);
                network.setOwner(p.getUniqueId());
                Bukkit.getScheduler().runTask(pm, new Runnable() {

                    @Override
                    public void run() {
                        WaygateManager.getManager().changeGateNetwork(currentWaygate, network, true);
                        mm.openWaygateSettingsMenu();
                    }

                });
            } else {
                Bukkit.getScheduler().runTask(pm, new Runnable() {

                    @Override
                    public void run() {
                        p.closeInventory();
                        Msg.NETWORK_CREATE_GLOBAL_UNIQUE.sendTo(p);
                    }

                });
                Bukkit.getScheduler().runTaskLater(pm, new Runnable() {
                    @Override
                    public void run() {
                        new MenuManager(p, currentWaygate).openWaygateSettingsMenu();
                    }
                }, 40L);
            }
        }
        super.onInventoryClick(clickEvent);
    }

    @Override
    public void buildMenu() {
        initMenu();

        // Add Private Gate Type
        addPrivateToMenu(0);

        int index = 1;
        // Add Invite Gate Type
        if (p.hasPermission("wg.assign.network.invite")) {
            addInviteToMenu(index);
            index += 1;
        }

        // Add Global Gate Type
        if (p.hasPermission("wg.assign.network.global")) {
            addGlobalToMenu(index);
        }

        addCancelToMenu();
    }

    private void addPrivateToMenu(int index) {
        addItemToMenu(index, Material.RAIL, Msg.MENU_TITLE_NETWORK_TYPE_PRIVATE.toString(), "Private");
    }

    private void addInviteToMenu(int index) {
        addItemToMenu(index, Material.DETECTOR_RAIL, Msg.MENU_TITLE_NETWORK_TYPE_INVITE.toString(), "Invite");
    }

    private void addGlobalToMenu(int index) {
        addItemToMenu(index, Material.POWERED_RAIL, Msg.MENU_TITLE_NETWORK_TYPE_GLOBAL.toString(), "Global");
    }
}
