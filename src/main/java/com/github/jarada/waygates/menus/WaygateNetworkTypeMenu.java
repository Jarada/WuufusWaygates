package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.data.Network;
import com.github.jarada.waygates.types.MenuSize;
import com.github.jarada.waygates.types.NetworkType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class WaygateNetworkTypeMenu extends Menu {

    private final String name;

    WaygateNetworkTypeMenu(MenuManager mm, Player p, Gate currentWaygate, String name) {
        super(mm, p, currentWaygate);
        this.name = name;
        setup();
    }

    @Override
    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        if (optionNames[slot].equals("Close")) {
            Bukkit.getScheduler().runTask(pm, () -> mm.openWaygateNetworkMenu());
        } else if (optionNames[slot].matches("Private|Invite|Fixed|Global")) {
            // We have one of the network types
            NetworkType type = NetworkType.GLOBAL;
            if (optionNames[slot].equals("Private"))
                type = NetworkType.PRIVATE;
            else if (optionNames[slot].equals("Invite"))
                type = NetworkType.INVITE;
            else if (optionNames[slot].equals("Fixed"))
                type = NetworkType.FIXED;

            if (mm.isNetworkNameUnique(name)) {
                Network network = new Network(name, type);
                network.setOwner(p.getUniqueId());
                Bukkit.getScheduler().runTask(pm, () -> {
                    WaygateManager.getManager().changeGateNetwork(currentWaygate, network, true);
                    mm.openWaygateSettingsMenu();
                });
            } else {
                Bukkit.getScheduler().runTask(pm, () -> {
                    p.closeInventory();
                    Msg.NETWORK_CREATE_GLOBAL_UNIQUE.sendTo(p);
                });
                Bukkit.getScheduler().runTaskLater(pm, () -> new MenuManager(p, currentWaygate).openWaygateSettingsMenu(), 40L);
            }
        }
        super.onInventoryClick(clickEvent);
    }

    @Override
    public int getDesiredMenuSize() {
        return MenuSize.COMPACT.getMenuSize();
    }

    @Override
    public void buildMenu() {
        initMenu();
        int index = 0;

        // Add Private Gate Type
        if (p.hasPermission("wg.create.network.private")) {
            addPrivateToMenu();
            index += 1;
        }

        // Add Invite Gate Type
        if (p.hasPermission("wg.create.network.invite")) {
            addInviteToMenu(index);
            index += 1;
        }

        // Add Fixed Gate Type
        if (p.hasPermission("wg.create.network.fixed")) {
            addFixedToMenu(index);
            index += 1;
        }

        // Add Global Gate Type
        if (p.hasPermission("wg.create.network.global")) {
            addGlobalToMenu(index);
        }

        addCancelToMenu();
    }

    private void addPrivateToMenu() {
        addItemToMenu(0, Material.RAIL, Msg.MENU_TITLE_NETWORK_TYPE_PRIVATE.toString(), "Private");
    }

    private void addInviteToMenu(int index) {
        addItemToMenu(index, Material.DETECTOR_RAIL, Msg.MENU_TITLE_NETWORK_TYPE_INVITE.toString(), "Invite");
    }

    private void addFixedToMenu(int index) {
        addItemToMenu(index, Material.ACTIVATOR_RAIL, Msg.MENU_TITLE_NETWORK_TYPE_FIXED.toString(), "Fixed");
    }

    private void addGlobalToMenu(int index) {
        addItemToMenu(index, Material.POWERED_RAIL, Msg.MENU_TITLE_NETWORK_TYPE_GLOBAL.toString(), "Global");
    }
}
