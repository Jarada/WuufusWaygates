package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Network;
import com.github.jarada.waygates.util.Util;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class MenuManager implements Listener {

    private PluginMain                  pm;
    private WaygateManager              wm;
    private DataManager                 dm;

    private Menu                activeMenu;
    private Inventory      activeInventory;

    private Player                  player;
    private Gate            currentWaygate;

    public MenuManager(Player player, Gate currentWaygate) {
        this.player = player;
        this.currentWaygate = currentWaygate;
        pm = PluginMain.getPluginInstance();
        wm = WaygateManager.getManager();
        dm = DataManager.getManager();
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
            if (Arrays.equals(closeEvent.getInventory().getContents(), activeMenu.optionIcons) && player == closeEvent.getPlayer()) {
                activeMenu.onInventoryClose(closeEvent);
                Bukkit.getScheduler().runTask(pm, new Runnable() {

                    @Override
                    public void run() {
                        destroy();
                    }

                });
            }
        }
    }

    private void open(@NotNull Menu menu) {
        player.setMetadata("InMenu", new FixedMetadataValue(PluginMain.getPluginInstance(), true));
        activeMenu = menu;

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
        player.removeMetadata("InMenu", pm);
        HandlerList.unregisterAll(this);

        pm = null;
        wm = null;
        player = null;
        currentWaygate = null;

        activeMenu = null;
        activeInventory = null;
    }

    private boolean canBypass() {
        return player.hasPermission("wg.bypass");
    }

    private boolean isOwner() {
        return currentWaygate.getOwner().equals(player.getUniqueId());
    }

    private ArrayList<Gate> loadAccessList() {
        ArrayList<Gate> accessList = (!currentWaygate.getNetwork().isVoid() || canBypass()) ?
                wm.getConnectedGates(currentWaygate) : new ArrayList<>();
        Iterator<Gate> gateIterator = accessList.iterator();
        while (gateIterator.hasNext()) {
            Gate accessGate = gateIterator.next();

            // Remove Hidden Gates if not owner or bypass
            if (accessGate.isOwnerHidden() && !(isOwner() || canBypass()))
                gateIterator.remove();

        }
        return accessList;
    }

    public void openWaygateMenu() {
        if (currentWaygate == null)
            return;

        if (currentWaygate.getDestination() != null) {
            Gate accessGate = currentWaygate.getDestination();
            if (accessGate.isOwnerHidden() && !(isOwner() || canBypass())) {
                // No Gates To Show
                open(new WaygateGateMenu(this, player, currentWaygate, new ArrayList<>()));
            } else {
                // Add Single Gate
                ArrayList<Gate> accessList = new ArrayList<>();
                accessList.add(accessGate);
                open(new WaygateGateMenu(this, player, currentWaygate, accessList));
            }
        } else {
            open(new WaygateGateMenu(this, player, currentWaygate, loadAccessList()));
        }
    }

    public void openWaygateSettingsMenu() {
        if (currentWaygate == null)
            return;

        open(new WaygateSettingsMenu(this, player, currentWaygate));
    }

    public void openWaygateDestinationMenu() {
        if (currentWaygate == null)
            return;

        open(new WaygateDestinationMenu(this, player, currentWaygate, loadAccessList()));
    }

    public void openWaygateNetworkMenu() {
        if (currentWaygate == null)
            return;

        ArrayList<Network> networks = new ArrayList<>(Network.systemNetworks());
        Iterator<Network> networkIterator = networks.iterator();
        while (networkIterator.hasNext()){
            Network network = networkIterator.next();
            String networkKey = network.getSysUuid().replace("sys_", "");
            if (!currentWaygate.getNetwork().equals(network) && !player.hasPermission(String.format("wg.network.%s", networkKey)))
                networkIterator.remove();
        }

        networks.addAll(wm.getCustomNetworks(player, currentWaygate));
        open(new WaygateNetworkMenu(this, player, currentWaygate, networks));
    }

    public void openWaygateNetworkTypeMenu(String name) {
        if (currentWaygate == null)
            return;

        open(new WaygateNetworkTypeMenu(this, player, currentWaygate, name));
    }

    public void openWaygateNetworkManageMenu() {
        if (currentWaygate == null)
            return;

        open(new WaygateNetworkManageMenu(this, player, currentWaygate));
    }

}
