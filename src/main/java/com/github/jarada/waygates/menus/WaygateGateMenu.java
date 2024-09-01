package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.*;
import com.github.jarada.waygates.types.GateActivationResult;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class WaygateGateMenu extends WaygateAccessMenu {

    public WaygateGateMenu(MenuManager mm, Player p, Gate currentWaygate, List<Gate> accessList) {
        super(mm, p, currentWaygate, accessList);
        setup();
    }

    public WaygateGateMenu(MenuManager mm, Player p, Controller controller, List<Gate> accessList) {
        super(mm, p, controller, accessList, true);
        setup();
    }

    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        if (optionWaygates[slot] != null) {
            Gate selectedGate = optionWaygates[slot];
            Bukkit.getScheduler().runTask(pm, () -> {
                if (currentWaygate.isActive())
                    currentWaygate.deactivate();
                GateActivationResult result = currentWaygate.activate(selectedGate);
                if (result == GateActivationResult.RESULT_ACTIVATED || currentWaygate.isAlwaysOn())
                    mm.saveUpdateToGate();
                p.closeInventory();
                if (result == GateActivationResult.RESULT_NOT_INTACT) {
                    WaygateManager.getManager().destroyWaygate(p, currentWaygate,
                            new BlockLocation(currentWaygate.getCenterBlock().getLocation()));
                } else if (result == GateActivationResult.RESULT_NOT_FOUND) {
                    Msg.GATE_EXIT_FAILURE.sendTo(p);
                } else if (result == GateActivationResult.RESULT_ACTIVATED && DataManager.getManager().WG_KEY_CONSUMES &&
                        !DataManager.getManager().WG_KEY_PERMANENT && !p.hasPermission("wg.keep.key")) {
                    ItemStack is = p.getInventory().getItemInMainHand();
                    if (is.isSimilar(DataManager.getManager().getCraftableItemStack(CraftableWaygateItem.WAYGATE_KEY)) ||
                            DataManager.getManager().isLockKeyValid(currentWaygate, is)) {
                        is.setAmount(is.getAmount() - 1);
                        p.getInventory().setItemInMainHand(is);
                    }
                }
            });
        } else {
            if (optionNames[slot].equals("Settings")) {
                Bukkit.getScheduler().runTask(pm, () -> mm.openWaygateSettingsMenu());
            } else if (optionNames[slot].equals("Reconfigure")) {
                Bukkit.getScheduler().runTask(pm, () -> mm.openControllerConfigureMenu());
            } else if (optionNames[slot].equals("Deactivate")) {
                Bukkit.getScheduler().runTask(pm, () -> {
                    if (currentWaygate.isActive())
                        currentWaygate.deactivate(true);
                    mm.close();
                });
            } else if (optionNames[slot].equals("Close")) {
                Bukkit.getScheduler().runTask(pm, () -> {
                    mm.close();
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
        addGateIconToMenu();
        addNetworkToMenu();

        if (page > 1) {
            addPreviousToMenu();
        }

        if (accessList.size() > getPageSize()) {
            addPageToMenu();
        }

        if (accessList.size() > page * getPageSize()) {
            addNextToMenu();
        }

        if (currentWaygate.isActive())
            addDeactivateGateToMenu();

        if (currentController != null && (currentController.getOwner().equals(p.getUniqueId()) || p.hasPermission("wg.admin")))
            addItemToMenu(currentWaygate.isActive() ? getActionSlot(6) : getActionSlot(7), Material.LEVER, Msg.MENU_TITLE_RECONFIGURE_CONTROLLER.toString(), "Reconfigure");
        else if (currentController == null && (currentWaygate.getOwner().equals(p.getUniqueId()) || p.hasPermission("wg.admin")))
            addItemToMenu(currentWaygate.isActive() ? getActionSlot(6) : getActionSlot(7), Material.LEVER, Msg.MENU_TITLE_SETTINGS.toString(), "Settings");

        addCloseToMenu();
    }

    private void addGateIconToMenu() {
        setOption(getActionSlot(1), currentWaygate);
        optionWaygates[getActionSlot(1)] = null;
    }

    private void addNetworkToMenu() {
        addNetworkToMenu(getActionSlot(2), currentWaygate.getNetwork(), false);
    }

    private void addDeactivateGateToMenu() {
        Material icon = Material.ACACIA_DOOR; // 1.15 support
        if (Arrays.stream(Material.values()).anyMatch(t -> t.name().equals("CRIMSON_DOOR"))) {
            icon = Material.CRIMSON_DOOR;
        }
        addItemToMenu(getActionSlot(7), icon, Msg.MENU_TITLE_DEACTIVATE.toString(), "Deactivate");
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
