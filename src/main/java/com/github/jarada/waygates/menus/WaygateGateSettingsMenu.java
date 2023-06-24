package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.callbacks.WaygateOwnerChangeCallback;
import com.github.jarada.waygates.callbacks.WaygateRenameCallback;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.listeners.ChatListener;
import com.github.jarada.waygates.types.MenuSize;
import com.github.jarada.waygates.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class WaygateGateSettingsMenu extends Menu {

    public WaygateGateSettingsMenu(MenuManager mm, Player p, Gate currentWaygate) {
        super(mm, p, currentWaygate);
        setup();
    }

    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        switch (optionNames[slot]) {
            case "Close":
                Bukkit.getScheduler().runTask(pm, () -> mm.openWaygateMenu());
                break;
            case "Name":
                Bukkit.getScheduler().runTask(pm, () -> {
                    p.closeInventory();
                    new ChatListener(new WaygateRenameCallback(p, currentWaygate));
                });
                break;
            case "Owner":
                Bukkit.getScheduler().runTask(pm, () -> {
                    p.closeInventory();
                    new ChatListener(new WaygateOwnerChangeCallback(p, currentWaygate));
                });
                break;
            case "Network":
                Bukkit.getScheduler().runTask(pm, () -> mm.openWaygateNetworkMenu());
                break;
            case "Destination":
                Bukkit.getScheduler().runTask(pm, () -> mm.openWaygateDestinationMenu());
                break;
            default:
                if (optionNames[slot].equals("Private")) {
                    currentWaygate.setOwnerPrivate(!currentWaygate.isOwnerPrivate());
                    mm.saveUpdateToGate();
                } else if (optionNames[slot].equals("Hidden")) {
                    currentWaygate.setOwnerHidden(!currentWaygate.isOwnerHidden());
                    mm.saveUpdateToGate();
                } else if (optionNames[slot].equals("AlwaysOn")) {
                    currentWaygate.setAlwaysOn(!currentWaygate.isAlwaysOn());
                    mm.saveUpdateToGate();
                } else if (optionNames[slot].equals("Effect")) {
                    currentWaygate.loopActivationEffect();
                    mm.saveUpdateToGate();
                }
                super.onInventoryClick(clickEvent);
                break;
        }
    }

    @Override
    public int getDesiredMenuSize() {
        return MenuSize.COMPACT.getMenuSize();
    }

    @Override
    public void buildMenu() {
        initMenu();

        addNameToMenu();
        addNetworkToMenu();

        int slot = 2;
        if (p.hasPermission("wg.assign.destination")) {
            addFixedDestinationToMenu(slot);
            slot += 1;
        }

        if (p.hasPermission("wg.assign.private")) {
            addPrivateToMenu(slot);
            slot += 1;
        }

        if (p.hasPermission("wg.assign.hidden") && currentWaygate.getNetwork().canAssignHiddenToGates(p.getUniqueId())) {
            addHiddenToMenu(slot);
            slot += 1;
        }

        if (p.hasPermission("wg.assign.alwayson")) {
            addAlwaysOnToMenu(slot);
            slot += 1;
        }

        if (p.hasPermission("wg.assign.effect")) {
            addEffectToMenu(slot);
        }

        addGateOwnerToMenu(true);
        addCloseToMenu();
    }

    void addNameToMenu() {
        List<String> lore = new ArrayList<>();
        lore.add(Util.color(Msg.MENU_TEXT_EDITABLE.toString(Util.stripColor(currentWaygate.getName()))));
        addItemToMenu(0, Material.NAME_TAG, Msg.MENU_TITLE_NAME.toString(), "Name", lore);
    }

    void addNetworkToMenu() {
        Material icon = currentWaygate.getNetwork().getIcon();
        List<String> lore = new ArrayList<>();
        lore.add(Util.color(Msg.MENU_TEXT_EDITABLE.toString(Util.stripColor(currentWaygate.getNetwork().getName()))));
        addItemToMenu(1, icon, Msg.MENU_TITLE_NETWORK.toString(), "Network", lore);
    }

    void addFixedDestinationToMenu(int slot) {
        List<String> lore = new ArrayList<>();
        if (currentWaygate.getFixedDestination() != null)
            lore.add(Util.color(Msg.MENU_TEXT_DESTINATION_SET.toString(Util.stripColor(currentWaygate.getFixedDestination().getName()))));
        else
            lore.add(Util.color(Msg.MENU_TEXT_DESTINATION_UNSET.toString()));
        addItemToMenu(slot, Material.ENDER_PEARL, Msg.MENU_TITLE_DESTINATION.toString(), "Destination", lore);
    }

    void addPrivateToMenu(int slot) {
        List<String> lore = new ArrayList<>();
        lore.add(Util.color(currentWaygate.isOwnerPrivate() ? Msg.MENU_TEXT_PRIVATE_SET.toString() :
                Msg.MENU_TEXT_PRIVATE_UNSET.toString()));
        Msg[] privateLore = {Msg.MENU_LORE_PRIVATE_1, Msg.MENU_LORE_PRIVATE_2, Msg.MENU_LORE_PRIVATE_3};
        for (Msg msg : privateLore)
            if (msg.toString().length() > 0)
                lore.add(Util.color(msg.toString()));
        addItemToMenu(slot, Material.OAK_FENCE_GATE, Msg.MENU_TITLE_PRIVATE.toString(), "Private", lore);
    }

    void addHiddenToMenu(int slot) {
        List<String> lore = new ArrayList<>();
        lore.add(Util.color(currentWaygate.isOwnerHidden() ? Msg.MENU_TEXT_HIDDEN_SET.toString() :
                Msg.MENU_TEXT_HIDDEN_UNSET.toString()));
        Msg[] hiddenLore = {Msg.MENU_LORE_HIDDEN_1, Msg.MENU_LORE_HIDDEN_2, Msg.MENU_LORE_HIDDEN_3};
        for (Msg msg : hiddenLore)
            if (msg.toString().length() > 0)
                lore.add(Util.color(msg.toString()));
        addItemToMenu(slot, Material.IRON_DOOR, Msg.MENU_TITLE_HIDDEN.toString(), "Hidden", lore);
    }

    void addAlwaysOnToMenu(int slot) {
        List<String> lore = new ArrayList<>();
        lore.add(Util.color(currentWaygate.isAlwaysOn() ? Msg.MENU_TEXT_ALWAYS_ON_SET.toString() :
                Msg.MENU_TEXT_ALWAYS_ON_UNSET.toString()));
        Msg[] alwaysOnLore = {Msg.MENU_LORE_ALWAYS_ON_1, Msg.MENU_LORE_ALWAYS_ON_2, Msg.MENU_LORE_ALWAYS_ON_3};
        for (Msg msg : alwaysOnLore)
            if (msg.toString().length() > 0)
                lore.add(Util.color(msg.toString()));
        addItemToMenu(slot, Material.FLINT_AND_STEEL, Msg.MENU_TITLE_ALWAYS_ON.toString(), "AlwaysOn", lore);
    }

    void addEffectToMenu(int slot) {
        List<String> lore = new ArrayList<>();
        lore.add(Util.color(currentWaygate.getActivationEffect().toString()));
        Msg[] effectLore = {Msg.MENU_LORE_ACTIVATION_EFFECT_1, Msg.MENU_LORE_ACTIVATION_EFFECT_2, Msg.MENU_LORE_ACTIVATION_EFFECT_3};
        for (Msg msg : effectLore)
            if (msg.toString().length() > 0)
                lore.add(Util.color(msg.toString()));
        addItemToMenu(slot, Material.ENCHANTING_TABLE, Msg.MENU_TITLE_ACTIVATION_EFFECT.toString(), "Effect", lore);
    }

}
