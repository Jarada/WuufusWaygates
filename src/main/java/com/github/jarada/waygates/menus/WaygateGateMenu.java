package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.BlockLocation;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class WaygateGateMenu extends WaygateAccessMenu {

    public WaygateGateMenu(MenuManager mm, Player p, Gate currentWaygate, List<Gate> accessList) {
        super(mm, p, currentWaygate, accessList);
        setup();
    }

    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        if (optionWaygates[slot] != null) {
            Gate selectedGate = optionWaygates[slot];
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    if (currentWaygate.isActive())
                        currentWaygate.deactivate();
                    boolean success = currentWaygate.activate(selectedGate.getExit());
                    if (success)
                        mm.saveUpdateToGate();
                    p.closeInventory();
                    if (!success) {
                        WaygateManager.getManager().destroyWaygate(p, currentWaygate,
                                new BlockLocation(currentWaygate.getCenterBlock().getLocation()));
                    }
                }

            });
        } else {
            if (optionNames[slot].equals("Settings")) {
                Bukkit.getScheduler().runTask(pm, new Runnable() {

                    @Override
                    public void run() {
                        mm.openWaygateSettingsMenu();
                    }

                });
            } else if (optionNames[slot].equals("Close")) {
                Bukkit.getScheduler().runTask(pm, new Runnable() {

                    @Override
                    public void run() {
                        if (currentWaygate.isActive())
                            currentWaygate.deactivate();
                        mm.close();
                    }

                });
            } else {
                super.onInventoryClick(clickEvent);
            }
        }
    }

    public void buildMenu() {
        initMenu();
        buildGates();

        addGateOwnerToMenu(false);
        addNetworkToMenu();

        if (page > 1) {
            addPreviousToMenu();
        }

        if (accessList.size() > 9) {
            addPageToMenu();
        }

        if (accessList.size() > page * 9) {
            addNextToMenu();
        }

        if (currentWaygate.getOwner().equals(p.getUniqueId()) || p.hasPermission("wg.admin")) {
            addItemToMenu(16, Material.LEVER, Msg.MENU_TITLE_SETTINGS.toString(), "Settings");
        }

        addCloseToMenu();
    }

    private void addNetworkToMenu() {
        addNetworkToMenu(10, currentWaygate.getNetwork(), false);
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
