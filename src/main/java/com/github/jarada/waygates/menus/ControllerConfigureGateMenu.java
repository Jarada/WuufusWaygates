package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.data.Controller;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ControllerConfigureGateMenu extends WaygateAccessMenu {

    public ControllerConfigureGateMenu(MenuManager mm, Player p, Controller currentController, List<Gate> accessList) {
        super(mm, p, currentController, accessList, false);
        setup();
    }

    public void updateGates(List<Gate> accessList) {
        this.accessList = accessList;
        setup();
    }

    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        if (optionWaygates[slot] != null) {
            Gate selectedGate = optionWaygates[slot];
            pm.getLogger().info(String.format("Found gate %s", selectedGate.getName()));
            Bukkit.getScheduler().runTask(pm, () -> {
                currentController.setGate(selectedGate);
                mm.saveUpdateToController();
                p.closeInventory();
                Msg.CONTROLLER_CONFIGURED.sendTo(p, selectedGate.getName());
            });
        } else if (optionNames[slot].equals("Close")) {
            Bukkit.getScheduler().runTask(pm, () -> mm.openControllerConfigureMenu());
        } else {
            super.onInventoryClick(clickEvent);
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

        addControllerOwnerToMenu();
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
