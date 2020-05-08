package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.callbacks.WaygateNetworkInviteCallback;
import com.github.jarada.waygates.callbacks.WaygateNetworkOwnerChangeCallback;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.listeners.ChatListener;
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

public class WaygateNetworkManageMenu extends Menu {

    List<OfflinePlayer>         invites;
    private OfflinePlayer[]     optionInvites;

    WaygateNetworkManageMenu(MenuManager mm, Player p, Gate currentWaygate) {
        super(mm, p, currentWaygate);

        invites = new ArrayList<>();
        for (UUID invitedUser : currentWaygate.getNetwork().getInvitedUsers()) {
            OfflinePlayer invitedPlayer = Bukkit.getOfflinePlayer(invitedUser);
            if (invitedPlayer != null) {
                invites.add(invitedPlayer);
            }
        }

        setup();
    }

    @Override
    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        if (optionInvites[slot] != null) {
            OfflinePlayer offlinePlayer = optionInvites[slot];
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    currentWaygate.getNetwork().removeInvitedUser(offlinePlayer.getUniqueId());
                    mm.saveUpdateToNetwork();
                    buildMenu();
                    clickEvent.getInventory().setContents(optionIcons);
                    // TODO Future: Add Confirmation Dialog
                }

            });
        }

        if (optionNames[slot].equals("Close")) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    mm.openWaygateNetworkMenu();
                }

            });
        } else if (optionNames[slot].equals("Owner")) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    p.closeInventory();
                    new ChatListener(new WaygateNetworkOwnerChangeCallback(p, currentWaygate));
                }

            });
        } else if (optionNames[slot].equals("Invite")) {
            Bukkit.getScheduler().runTask(pm, new Runnable() {

                @Override
                public void run() {
                    p.closeInventory();
                    new ChatListener(new WaygateNetworkInviteCallback(p, currentWaygate));
                }

            });
        } else {
            super.onInventoryClick(clickEvent);
        }
    }

    @Override
    public void buildMenu() {
        initMenu();
        optionInvites = new OfflinePlayer[size];

        addNetworkOwnerToMenu();

        if (currentWaygate.getNetwork().isNetworkInvite())
            addInvitePlayerToMenu();

        for (int slot = 0; slot < 9; slot++) {
            int index = ((page - 1) * 9) + slot;

            if (index > invites.size() - 1)
                break;

            OfflinePlayer invitedPlayer = invites.get(index);
            addInvitedPlayerToMenu(slot, invitedPlayer);
        }

        if (page > 1) {
            addPreviousToMenu();
        }

        if (invites.size() > 9) {
            addPageToMenu();
        }

        if (invites.size() > page * 9) {
            addNextToMenu();
        }

        addCloseToMenu();
    }

    void addNetworkOwnerToMenu() {
        if (currentWaygate.getNetwork().getOwner() != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(currentWaygate.getNetwork().getOwner());
            if (owner != null) {
                ItemStack is = Util.getHead(owner);
                List<String> lore = new ArrayList<String>();
                lore.add(Util.color(Msg.MENU_TEXT_EDITABLE.toString(owner.getName())));
                Util.setItemNameAndLore(is, Util.color(Msg.MENU_TITLE_NETWORK_OWNER.toString()), lore);
                setOption(9, "Owner", is);
            }
        }
    }

    private void addInvitePlayerToMenu() {
        addItemToMenu(10, Material.WRITABLE_BOOK, Msg.MENU_TITLE_NETWORK_INVITE_ADD.toString(), "Invite");
    }

    private void addInvitedPlayerToMenu(int slot, OfflinePlayer invitedPlayer) {
        ItemStack is = Util.getHead(invitedPlayer);
        List<String> lore = new ArrayList<String>();
        lore.add(Msg.MENU_TEXT_EDITABLE.toString(invitedPlayer.getName()));
        Util.setItemNameAndLore(is, Util.color(Msg.MENU_TITLE_NETWORK_INVITE_EXISTING.toString()), lore);
        setOption(slot, invitedPlayer.getUniqueId().toString(), is);
        optionInvites[slot] = invitedPlayer;
    }

}
