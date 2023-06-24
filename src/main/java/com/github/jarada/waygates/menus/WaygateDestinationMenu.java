package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

//import com.github.jarada.waygates.tasks.TeleportTask;

public class WaygateDestinationMenu extends WaygateAccessMenu {

    public WaygateDestinationMenu(MenuManager mm, Player p, Gate currentWaygate, List<Gate> accessList) {
        super(mm, p, currentWaygate, accessList);
        setup();
    }

    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        if (optionWaygates[slot] != null) {
            Gate selectedGate = optionWaygates[slot];
            Bukkit.getScheduler().runTask(pm, () -> {
                currentWaygate.setFixedDestination(selectedGate);
                mm.saveUpdateToGate().openWaygateSettingsMenu();
            });
        } else {
            if (optionNames[slot].equals("Close")) {
                Bukkit.getScheduler().runTask(pm, mm::openWaygateSettingsMenu);
            } else if (optionNames[slot].equals("Clear")) {
                Bukkit.getScheduler().runTask(pm, () -> {
                    currentWaygate.setFixedDestination(null);
                    mm.saveUpdateToGate().openWaygateSettingsMenu();
                });
            } else {
                super.onInventoryClick(clickEvent);
            }
        }
    }

    @Override
    public void buildMenu() {
        initMenu();
        buildGates();

        if (page > 1) {
            addPreviousToMenu();
        }

        if (accessList.size() > getPageSize()) {
            addPageToMenu();
        }

        if (accessList.size() > page * getPageSize()) {
            addNextToMenu();
        }

        if (currentWaygate.getFixedDestination() != null) {
            addItemToMenu(getActionSlot(7), Material.BARRIER, Msg.MENU_TITLE_DESTINATION_CLEAR.toString(), "Clear");
        }

        addCloseToMenu();
    }

    public void setOption(int slot, String name, ItemStack icon) {
        super.setOption(slot, name, icon);
        optionWaygates[slot] = null;
    }

    public void setOption(int slot, Gate gate) {
        super.setOption(slot, gate);
        optionWaygates[slot] = gate;
    }

}
