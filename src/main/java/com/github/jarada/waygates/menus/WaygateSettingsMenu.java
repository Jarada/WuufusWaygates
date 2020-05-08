package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.callbacks.WaygateOwnerChangeCallback;
import com.github.jarada.waygates.callbacks.WaygateRenameCallback;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.listeners.ChatListener;
import com.github.jarada.waygates.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class WaygateSettingsMenu extends Menu {

    public WaygateSettingsMenu(MenuManager mm, Player p, Gate currentWaygate) {
        super(mm, p, currentWaygate);
        setup();
    }

    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        if (optionNames[slot].equals("Close")) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    mm.openWaygateMenu();
                }

            });
        } else if (optionNames[slot].equals("Name")) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    p.closeInventory();
                    new ChatListener(new WaygateRenameCallback(p, currentWaygate));
                }

            });
        } else if (optionNames[slot].equals("Owner")) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    p.closeInventory();
                    new ChatListener(new WaygateOwnerChangeCallback(p, currentWaygate));
                }

            });
        } else if (optionNames[slot].equals("Network")) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    mm.openWaygateNetworkMenu();
                }

            });
        } else if (optionNames[slot].equals("Destination")) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    mm.openWaygateDestinationMenu();
                }

            });
        } else {
            if (optionNames[slot].equals("Private")) {
                currentWaygate.setOwnerPrivate(!currentWaygate.isOwnerPrivate());
                mm.saveUpdateToGate();
            } else if (optionNames[slot].equals("Hidden")) {
                currentWaygate.setOwnerHidden(!currentWaygate.isOwnerHidden());
                mm.saveUpdateToGate();
            }
            super.onInventoryClick(clickEvent);
        }
    }

    @Override
    public void buildMenu() {
        initMenu();

        addNameToMenu();
        addNetworkToMenu();
        addDestinationToMenu();

        if (p.hasPermission("wg.assign.gate.private"))
            addPrivateToMenu();

        if (p.hasPermission("wg.assign.gate.hidden"))
            addHiddenToMenu();

        addOwnerToMenu(true);
        addCloseToMenu();
    }

    void addNameToMenu() {
        List<String> lore = new ArrayList<String>();
        lore.add(Util.color(Msg.MENU_TEXT_EDITABLE.toString(Util.stripColor(currentWaygate.getName()))));
        addItemToMenu(0, Material.NAME_TAG, Msg.MENU_TITLE_NAME.toString(), "Name", lore);
    }

    void addNetworkToMenu() {
        Material icon = currentWaygate.getNetwork().getIcon();
        List<String> lore = new ArrayList<String>();
        lore.add(Util.color(Msg.MENU_TEXT_EDITABLE.toString(Util.stripColor(currentWaygate.getNetwork().getName()))));
        addItemToMenu(1, icon, Msg.MENU_TITLE_NETWORK.toString(), "Network", lore);
    }

    void addDestinationToMenu() {
        List<String> lore = new ArrayList<String>();
        if (currentWaygate.getDestination() != null)
            lore.add(Util.color(Msg.MENU_TEXT_DESTINATION_SET.toString(Util.stripColor(currentWaygate.getDestination().getName()))));
        else
            lore.add(Util.color(Msg.MENU_TEXT_DESTINATION_UNSET.toString()));
        addItemToMenu(2, Material.ENDER_PEARL, Msg.MENU_TITLE_DESTINATION.toString(), "Destination", lore);
    }

    void addPrivateToMenu() {
        List<String> lore = new ArrayList<String>();
        lore.add(Util.color(currentWaygate.isOwnerPrivate() ? Msg.MENU_TEXT_PRIVATE_SET.toString() :
                Msg.MENU_TEXT_PRIVATE_UNSET.toString()));
        Msg[] privateLore = {Msg.MENU_LORE_PRIVATE_1, Msg.MENU_LORE_PRIVATE_2, Msg.MENU_LORE_PRIVATE_3};
        for (Msg msg : privateLore)
            if (msg.toString().length() > 0)
                lore.add(Util.color(msg.toString()));
        addItemToMenu(3, Material.OAK_FENCE_GATE, Msg.MENU_TITLE_PRIVATE.toString(), "Private", lore);
    }

    void addHiddenToMenu() {
        List<String> lore = new ArrayList<String>();
        lore.add(Util.color(currentWaygate.isOwnerHidden() ? Msg.MENU_TEXT_HIDDEN_SET.toString() :
                Msg.MENU_TEXT_HIDDEN_UNSET.toString()));
        Msg[] hiddenLore = {Msg.MENU_LORE_HIDDEN_1, Msg.MENU_LORE_HIDDEN_2, Msg.MENU_LORE_HIDDEN_3};
        for (Msg msg : hiddenLore)
            if (msg.toString().length() > 0)
                lore.add(Util.color(msg.toString()));
        addItemToMenu(4, Material.IRON_DOOR, Msg.MENU_TITLE_HIDDEN.toString(), "Hidden", lore);
    }

}
