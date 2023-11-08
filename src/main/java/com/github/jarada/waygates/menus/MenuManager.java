package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.Controller;
import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Network;
import com.github.jarada.waygates.types.MenuSize;
import com.github.jarada.waygates.util.ItemStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MenuManager implements Listener {

    private static final String IN_MENU = "InMenu";
    private static final Map<Player, MenuExpirable> expirableMap = new HashMap<>();

    private PluginMain                  pm;
    private WaygateManager              wm;
    private DataManager                 dm;

    private Menu                activeMenu;
    private Inventory      activeInventory;

    private Player                  player;
    private Controller   currentController;
    private Gate            currentWaygate;

    public MenuManager(Player player, Controller currentController) {
        this(player, currentController.getGate());
        this.currentController = currentController;
    }

    public MenuManager(Player player, Gate currentWaygate) {
        this.player = player;
        this.currentWaygate = currentWaygate;
        pm = PluginMain.getPluginInstance();
        wm = WaygateManager.getManager();
        dm = DataManager.getManager();
    }

    public MenuManager saveUpdateToController() {
        dm.saveController(currentController);
        return this;
    }

    public MenuManager saveUpdateToGate() {
        dm.saveWaygate(currentWaygate, false);
        return this;
    }

    public MenuManager saveUpdateToNetwork() {
        dm.saveNetwork(currentWaygate.getNetwork());
        return this;
    }

    public boolean isNetworkNameUnique(String name) {
        return wm.isNetworkNameUnique(name);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClick(InventoryClickEvent clickEvent) {
        if (activeMenu != null) {
            if (activeMenu.verifyInventoryClick(clickEvent)) {
                activeMenu.onInventoryClick(clickEvent);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onInventoryClose(InventoryCloseEvent closeEvent) {
        if (activeMenu != null) {
            if (ItemStackUtil.equals(closeEvent.getInventory().getContents(), activeMenu.optionIcons) && player == closeEvent.getPlayer()) {
                activeMenu.onInventoryClose(closeEvent);
                Bukkit.getScheduler().runTask(pm, this::destroy);
            }
        }
    }

    private void open(@NotNull Menu menu) {
        if (expirableMap.containsKey(player))
            expirableMap.get(player).expire();

        player.setMetadata(IN_MENU, new FixedMetadataValue(PluginMain.getPluginInstance(), true));
        activeMenu = menu;

        MenuSize menuSize = DataManager.getManager().MENU_SIZE;
        if (menuSize == MenuSize.RESIZE && activeInventory != null) {
            close();
            activeInventory = null;
            HandlerList.unregisterAll(this);
        }

        if (activeInventory != null) {
            activeInventory.clear();
            activeInventory.setContents(activeMenu.optionIcons);
        } else {
            Bukkit.getPluginManager().registerEvents(this, pm);
            activeInventory = Bukkit.createInventory(activeMenu.p, activeMenu.size, activeMenu.getMenuName());
            activeInventory.setContents(activeMenu.optionIcons);
            player.openInventory(activeInventory);
        }
    }

    public void close() {
        if (activeInventory != null) {
            player.closeInventory();
        }
    }

    private void destroy() {
        player.removeMetadata(IN_MENU, pm);
        HandlerList.unregisterAll(this);

        pm = null;
        wm = null;
        dm = null;
        player = null;
        currentController = null;
        currentWaygate = null;

        activeMenu = null;
        activeInventory = null;
    }

    private boolean canBypass() {
        return player.hasPermission("wg.bypass");
    }

    private boolean isOwner(Gate accessGate) {
        return accessGate.getOwner().equals(player.getUniqueId());
    }

    private List<Gate> loadAccessList() {
        List<Gate> accessList = (!currentWaygate.getNetwork().isVoid() || canBypass()) ?
                wm.getConnectedGates(currentWaygate, currentWaygate.getNetwork().isFixed()) :
                new ArrayList<>();
        // Remove Hidden Gates if not owner or bypass
        accessList.removeIf(accessGate -> accessGate.isOwnerHidden() && !(isOwner(accessGate) || canBypass()));
        return accessList;
    }

    private List<Gate> loadNetworkGatesList(Network network) {
        List<Gate> accessList = wm.getGatesInNetwork(network);
        List<Gate> localList = wm.getGatesNearLocation(currentController.getLocation(), DataManager.getManager().WG_CONTROLLER_DISTANCE);
        // Remove Hidden Gates if not owner or bypass
        accessList.removeIf(accessGate -> !localList.contains(accessGate) || (!isOwner(accessGate) && !canBypass()));
        return accessList;
    }

    private List<Network> loadNetworkList() {
        ArrayList<Network> networks = new ArrayList<>(Network.systemNetworks());
        if (currentController != null)
            networks.removeIf(network -> !player.hasPermission(String.format("wg.network.%s", network.getSysKey())));
        else
            networks.removeIf(network -> !currentWaygate.getNetwork().equals(network) && !player.hasPermission(String.format("wg.network.%s", network.getSysKey())));
        networks.addAll(wm.getCustomNetworks(player, currentWaygate));
        return networks;
    }

    public void openControllerConfigureMenu() {
        if (currentController == null)
            return;

        open(new ControllerConfigureNetworkMenu(this, player, currentController, loadNetworkList()));
    }

    public void openControllerConfigureGateMenu(Network chosenNetwork) {
        if (currentController == null)
            return;

        ControllerConfigureGateMenu menu = new ControllerConfigureGateMenu(this, player, currentController,
                (DataManager.getManager().WG_CONTROLLER_DISTANCE > 16) ? Collections.emptyList() : loadNetworkGatesList(chosenNetwork));
        open(menu);
        if (DataManager.getManager().WG_CONTROLLER_DISTANCE > 16) {
            menu.updateGates(loadNetworkGatesList(chosenNetwork));
            if (menu == activeMenu)
                activeInventory.setContents(menu.optionIcons);
        }
    }

    public void openWaygateMenu() {
        if (currentWaygate == null)
            return;

        if (currentWaygate.getFixedDestination() != null) {
            Gate accessGate = currentWaygate.getFixedDestination();
            if (accessGate.isOwnerHidden() && !(isOwner(accessGate) || canBypass())) {
                // No Gates To Show
                open((currentController != null) ?
                        new WaygateGateMenu(this, player, currentController, new ArrayList<>()) :
                        new WaygateGateMenu(this, player, currentWaygate, new ArrayList<>()));
            } else {
                // Add Single Gate
                ArrayList<Gate> accessList = new ArrayList<>();
                accessList.add(accessGate);
                open((currentController != null) ?
                        new WaygateGateMenu(this, player, currentController, accessList) :
                        new WaygateGateMenu(this, player, currentWaygate, accessList));
            }
        } else {
            open((currentController != null) ?
                    new WaygateGateMenu(this, player, currentController, loadAccessList()) :
                    new WaygateGateMenu(this, player, currentWaygate, loadAccessList()));
        }
    }

    public void openWaygateSettingsMenu() {
        if (currentWaygate == null)
            return;

        open(new WaygateGateSettingsMenu(this, player, currentWaygate));
    }

    public void openWaygateDestinationMenu() {
        if (currentWaygate == null)
            return;

        open(new WaygateDestinationMenu(this, player, currentWaygate, loadAccessList()));
    }

    public void openWaygateNetworkMenu() {
        if (currentWaygate == null)
            return;

        open(new WaygateNetworkMenu(this, player, currentWaygate, loadNetworkList()));
    }

    public void openWaygateNetworkTypeMenu(String name) {
        if (currentWaygate == null)
            return;

        open(new WaygateNetworkTypeMenu(this, player, currentWaygate, name));
    }

    public void openWaygateNetworkInviteMenu() {
        if (currentWaygate == null)
            return;

        open(new WaygateNetworkInviteMenu(this, player, currentWaygate));
    }

    public void openWaygateNetworkManageMenu() {
        if (currentWaygate == null)
            return;

        open(new WaygateNetworkSettingsMenu(this, player, currentWaygate));
    }

    /* Menu Expirable */

    public static void setExpirable(Player player, MenuExpirable expirable) {
        MenuManager.expirableMap.put(player, expirable);
    }

    public static void clearExpirable(Player player) {
        MenuManager.expirableMap.remove(player);
    }

}
