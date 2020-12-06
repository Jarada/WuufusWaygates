package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.callbacks.WaygateNetworkCreateCallback;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.data.Network;
import com.github.jarada.waygates.listeners.ChatListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.List;

public class WaygateNetworkMenu extends Menu {

    List<Network>       networkList;
    private Network[]   optionNetworks;

    public WaygateNetworkMenu(MenuManager mm, Player p, Gate currentWaygate, List<Network> networkList) {
        super(mm, p, currentWaygate);
        this.networkList = networkList;
        setup();
    }

    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        if (optionNetworks[slot] != null) {
            Network network = optionNetworks[slot];
            Bukkit.getScheduler().runTask(pm, () -> {
                WaygateManager.getManager().changeGateNetwork(currentWaygate, network, false);
                mm.openWaygateSettingsMenu();
            });
        }

        switch (optionNames[slot]) {
            case "Close":
                Bukkit.getScheduler().runTask(pm, () -> mm.openWaygateSettingsMenu());
                break;
            case "Create":
                Bukkit.getScheduler().runTask(pm, () -> {
                    p.closeInventory();
                    new ChatListener(new WaygateNetworkCreateCallback(p, currentWaygate));
                });
                break;
            case "Invite":
                Bukkit.getScheduler().runTask(pm, () -> mm.openWaygateNetworkInviteMenu());
                break;
            case "Manage":
                Bukkit.getScheduler().runTask(pm, () -> mm.openWaygateNetworkManageMenu());
                break;
            default:
                super.onInventoryClick(clickEvent);
                break;
        }
    }

    @Override
    public void buildMenu() {
        initMenu();
        optionNetworks = new Network[size];

        for (int slot = 0; slot < 9; slot++) {
            int index = ((page - 1) * 9) + slot;

            if (index > networkList.size() - 1)
                break;

            Network nw = networkList.get(index);
            addNetworkToMenu(slot, nw);
        }

        if (page > 1) {
            addPreviousToMenu();
        }

        if (networkList.size() > 9) {
            addPageToMenu();
        }

        if (networkList.size() > page * 9) {
            addNextToMenu();
        }

        if (Network.isAbleToCreateNetworks(p))
            addAddNetworkToMenu();

        if (!currentWaygate.getNetwork().isSystem() && (currentWaygate.getNetwork().getOwner().equals(p.getUniqueId()) || p.hasPermission("wg.admin"))) {
            addManageNetworkToMenu();
            if (currentWaygate.getNetwork().isInvite())
                addManageNetworkInvitesToMenu();
        }

        addCloseToMenu();
    }

    void addNetworkToMenu(int slot, Network nw) {
        super.addNetworkToMenu(slot, nw, true);
        optionNetworks[slot] = nw;
    }

    void addAddNetworkToMenu() {
        addItemToMenu(9, Material.WRITABLE_BOOK, Msg.MENU_TITLE_NETWORK_CREATE.toString(), "Create");
    }

    void addManageNetworkInvitesToMenu() {
        Material icon = Material.TORCH; // 1.13 support
        if (Arrays.stream(Material.values()).anyMatch(t -> t.name().equals("CAMPFIRE"))) {
            icon = Material.CAMPFIRE;
        }
        addItemToMenu(15, icon, Msg.MENU_TITLE_NETWORK_INVITE_MANAGE.toString(), "Invite");
    }

    void addManageNetworkToMenu() {
        addItemToMenu(16, Material.LEVER, Msg.MENU_TITLE_NETWORK_MANAGE.toString(), "Manage");
    }
}
