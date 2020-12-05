package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.callbacks.WaygateNetworkIconChangeCallback;
import com.github.jarada.waygates.callbacks.WaygateNetworkInviteCallback;
import com.github.jarada.waygates.callbacks.WaygateNetworkOwnerChangeCallback;
import com.github.jarada.waygates.callbacks.WaygateNetworkRenameCallback;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.listeners.ChatListener;
import com.github.jarada.waygates.listeners.IconListener;
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

public class WaygateNetworkInviteMenu extends Menu {

    List<OfflinePlayer>         invites;
    private OfflinePlayer[]     optionInvites;

    WaygateNetworkInviteMenu(MenuManager mm, Player p, Gate currentWaygate) {
        super(mm, p, currentWaygate);

        invites = new ArrayList<>();
        for (UUID invitedUser : currentWaygate.getNetwork().getInvitedUsers()) {
            OfflinePlayer invitedPlayer = Bukkit.getOfflinePlayer(invitedUser);
            invites.add(invitedPlayer);
        }

        setup();
    }

    @Override
    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        if (optionInvites[slot] != null) {
            OfflinePlayer offlinePlayer = optionInvites[slot];
            Bukkit.getScheduler().runTask(pm, () -> {
                currentWaygate.getNetwork().removeInvitedUser(offlinePlayer.getUniqueId());
                mm.saveUpdateToNetwork();
                buildMenu();
                clickEvent.getInventory().setContents(optionIcons);
                // TODO Future: Add Confirmation Dialog
            });
        }

        switch (optionNames[slot]) {
            case "Close":
                Bukkit.getScheduler().runTask(pm, () -> mm.openWaygateNetworkMenu());
                break;
            case "Invite":
                Bukkit.getScheduler().runTask(pm, () -> {
                    p.closeInventory();
                    new ChatListener(new WaygateNetworkInviteCallback(p, currentWaygate));
                });
                break;
            default:
                super.onInventoryClick(clickEvent);
                break;
        }
    }

    @Override
    public void buildMenu() {
        initMenu();
        optionInvites = new OfflinePlayer[size];

        addNetworkOwnerToMenu(9, false);

        if (currentWaygate.getNetwork().isInvite() || currentWaygate.getNetwork().isFixed())
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
    
    private void addInvitePlayerToMenu() {
        addItemToMenu(10, Material.WRITABLE_BOOK, Msg.MENU_TITLE_NETWORK_INVITE_ADD.toString(), "Invite");
    }

    private void addInvitedPlayerToMenu(int slot, OfflinePlayer invitedPlayer) {
        List<String> lore = new ArrayList<>();
        lore.add(Util.color(Msg.MENU_TEXT_EDITABLE.toString(invitedPlayer.getName())));
        ItemStack is = Util.getHead(invitedPlayer, Util.color(Msg.MENU_TITLE_NETWORK_INVITE_EXISTING.toString()), lore);
        setOption(slot, invitedPlayer.getUniqueId().toString(), is);
        optionInvites[slot] = invitedPlayer;
    }

}
