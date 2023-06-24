package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.callbacks.WaygateNetworkIconChangeCallback;
import com.github.jarada.waygates.callbacks.WaygateNetworkInviteCallback;
import com.github.jarada.waygates.callbacks.WaygateNetworkOwnerChangeCallback;
import com.github.jarada.waygates.callbacks.WaygateNetworkRenameCallback;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.listeners.ChatListener;
import com.github.jarada.waygates.listeners.IconListener;
import com.github.jarada.waygates.types.MenuSize;
import com.github.jarada.waygates.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WaygateNetworkSettingsMenu extends Menu {

    WaygateNetworkSettingsMenu(MenuManager mm, Player p, Gate currentWaygate) {
        super(mm, p, currentWaygate);
        setup();
    }

    @Override
    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        switch (optionNames[slot]) {
            case "Close":
                Bukkit.getScheduler().runTask(pm, () -> mm.openWaygateNetworkMenu());
                break;
            case "Owner":
                Bukkit.getScheduler().runTask(pm, () -> {
                    p.closeInventory();
                    new ChatListener(new WaygateNetworkOwnerChangeCallback(p, currentWaygate));
                });
                break;
            case "Name":
                Bukkit.getScheduler().runTask(pm, () -> {
                    p.closeInventory();
                    new ChatListener(new WaygateNetworkRenameCallback(p, currentWaygate));
                });
                break;
            case "Icon":
                Bukkit.getScheduler().runTask(pm, () -> {
                    p.closeInventory();
                    new IconListener(new WaygateNetworkIconChangeCallback(p, currentWaygate));
                });
                break;
            default:
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

        addNetworkOwnerToMenu(1, true);

        if (!currentWaygate.getNetwork().isSystemIcon())
            addNetworkIconToMenu();

        addCloseToMenu();
    }

    void addNameToMenu() {
        List<String> lore = new ArrayList<>();
        lore.add(Util.color(Msg.MENU_TEXT_EDITABLE.toString(Util.stripColor(currentWaygate.getNetwork().getName()))));
        addItemToMenu(0, Material.NAME_TAG, Msg.MENU_TITLE_NETWORK_NAME.toString(), "Name", lore);
    }

    void addNetworkIconToMenu() {
        addItemToMenu(2, currentWaygate.getNetwork().getIcon(), Msg.MENU_TITLE_NETWORK_ICON.toString(), "Icon", null);
    }

}
