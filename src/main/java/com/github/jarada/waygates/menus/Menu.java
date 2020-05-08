package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.data.Network;
import com.github.jarada.waygates.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Menu {

    PluginMain    pm;
    MenuManager   mm;

    Player        p;
    Gate          currentWaygate;

    int           page;
    int           size;
    String[]      optionNames;
    ItemStack[]   optionIcons;

    Menu(MenuManager mm, Player p, Gate currentWaygate) {
        pm = PluginMain.getPluginInstance();

        this.mm = mm;
        this.p = p;
        this.currentWaygate = currentWaygate;
    }

    void setup() {
        page = 1;
        buildMenu();
    }

    protected void destroy() {
        p.removeMetadata("InMenu", pm);

        pm = null;
        p = null;
        currentWaygate = null;
        optionNames = null;
        optionIcons = null;
    }

    String getMenuName() {
        return currentWaygate.getName();
    }

    void onInventoryClick(InventoryClickEvent clickEvent) {
        final int slot = clickEvent.getRawSlot();

        if (optionNames[slot].equals("Previous")) {
            page--;
        } else if (optionNames[slot].equals("Page") && page != 1) {
            page = 1;
        } else if (optionNames[slot].equals("Next")) {
            page++;
        }

        buildMenu();
        clickEvent.getInventory().setContents(optionIcons);
    }

    boolean verifyInventoryClick(InventoryClickEvent clickEvent) {
        if (!Arrays.equals(clickEvent.getInventory().getContents(), optionIcons)
                || p != (Player) clickEvent.getWhoClicked())
            return false;

        if (!clickEvent.isCancelled())
            clickEvent.setCancelled(true);

        if (!clickEvent.isLeftClick())
            return false;

        final int slot = clickEvent.getRawSlot();

        if (slot < 0 || slot >= size || optionNames[slot] == null)
            return false;

        return true;
    }

    void onInventoryClose(InventoryCloseEvent closeEvent) {
        Bukkit.getScheduler().runTask(pm, new Runnable() {

            @Override
            public void run() {
                destroy();
            }

        });
    }

    public abstract void buildMenu();

    void initMenu() {
        size = 18;
        optionNames = new String[size];
        optionIcons = new ItemStack[size];
    }

    void loadMenus() {

    }

    void addItemToMenu(int slot, Material icon, String name, String optionName) {
        addItemToMenu(slot, icon, name, optionName, null);
    }

    void addItemToMenu(int slot, Material icon, String name, String optionName, List<String> lore) {
        ItemStack is = new ItemStack(icon, 1);
        Util.setItemNameAndLore(is, Util.color(name), lore);
        setOption(slot, optionName, is);
    }

    void addGateOwnerToMenu(boolean editable) {
        if (currentWaygate.getOwner() != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(currentWaygate.getOwner());
            if (owner != null) {
                ItemStack is = Util.getHead(owner);
                List<String> lore = new ArrayList<String>();
                lore.add(Util.color(editable ? Msg.MENU_TEXT_EDITABLE.toString(owner.getName()) : Msg.MENU_TEXT_STANDARD.toString(owner.getName())));
                Util.setItemNameAndLore(is, Util.color(Msg.MENU_TITLE_GATE_OWNER.toString()), lore);
                setOption(9, "Owner", is);
            }
        }
    }

    void addNetworkToMenu(int slot, Network nw, boolean showSelected) {
        String displayName = (nw.isSystem()) ?
                Msg.MENU_COLOR_SYSTEM_NETWORK.toString() + Util.stripColor(nw.getName()) :
                Msg.MENU_COLOR_NETWORK.toString() + Util.stripColor(nw.getName());
        int gateCount = WaygateManager.getManager().countOfGatesInNetwork(p, nw, false);

        List<String> lore = new ArrayList<String>();
        if (nw.isSystem()) {
            if (nw.equals(Network.getVoidNetwork()))
                lore.add(Util.color(Msg.NETWORK_SYSTEM_VOID.toString()));
            else
                lore.add(Util.color(Msg.NETWORK_SYSTEM.toString()));
        } else if (nw.isNetworkPrivate()) {
            lore.add(Util.color(Msg.NETWORK_PRIVATE.toString()));
        } else if (nw.isNetworkInvite()) {
            lore.add(Util.color(Msg.NETWORK_INVITE.toString()));
        } else {
            lore.add(Util.color(Msg.NETWORK_GLOBAL.toString()));
        }

        if (nw.getOwner() != null) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(nw.getOwner());
            lore.add(Util.color(Msg.MENU_TEXT_OWNER.toString(p.getName())));
        }
        lore.add(Util.color(String.format("&f %d %s", gateCount, Msg.WORD_GATES.toString())));

        optionNames[slot] = Util.color(displayName);

        if (currentWaygate != null && currentWaygate.getNetwork().equals(nw) && showSelected)
            optionNames[slot] = Util.color("&a* ") + optionNames[slot];

        ItemStack icon = new ItemStack(nw.getIcon(), 1);
        optionIcons[slot] = Util.setItemNameAndLore(icon, optionNames[slot], lore);
    }

    void addPreviousToMenu() {
        addItemToMenu(12, Material.PAPER, Msg.MENU_TITLE_PREVIOUS.toString(), "Previous");
    }

    void addPageToMenu() {
        addItemToMenu(13, Material.BOOK, Msg.MENU_TITLE_PAGE.toString(), "Page");
    }

    void addNextToMenu() {
        addItemToMenu(14, Material.PAPER, Msg.MENU_TITLE_NEXT.toString(), "Next");
    }

    void addCancelToMenu() {
        addItemToMenu(17, Material.DARK_OAK_DOOR, Msg.MENU_TITLE_CANCEL.toString(), "Cancel");
    }

    void addCloseToMenu() {
        addItemToMenu(17, Material.DARK_OAK_DOOR, Msg.MENU_TITLE_CLOSE.toString(), "Close");
    }

    public void setOption(int slot, String name, ItemStack icon) {
        optionNames[slot] = name;
        optionIcons[slot] = icon;
    }

    public void setOption(int slot, Gate gate) {
        Location loc = gate.getExit().getLocation();
        String displayName = Msg.MENU_COLOR_GATE.toString() + gate.getName();

        if (gate.isOwnerPrivate() && gate.isOwnerHidden())
            displayName += String.format(" %s", Msg.MENU_GATE_RESTRICTED.toString());
        else if (gate.isOwnerPrivate())
            displayName += String.format(" %s", Msg.MENU_GATE_PRIVATE.toString());
        else if (gate.isOwnerHidden())
            displayName += String.format(" %s", Msg.MENU_GATE_HIDDEN.toString());

        List<String> lore = new ArrayList<String>();
        lore.add(Util.color(String.format("&f&o(%s)", loc.getWorld().getName())));
        lore.add(Util.color(String.format("&aX: &f%s", loc.getBlockX())));
        lore.add(Util.color(String.format("&aY: &f%s", loc.getBlockY())));
        lore.add(Util.color(String.format("&aZ: &f%s", loc.getBlockZ())));

        if (gate.getDescription() != null && gate.getDescription().length() > 0)
            lore.addAll(Arrays.asList(Util.getWrappedLore(gate.getDescription(), 25)));

        optionNames[slot] = Util.color(displayName);

        ItemStack icon = new ItemStack(gate.getIcon(), 1);
        optionIcons[slot] = Util.setItemNameAndLore(icon, optionNames[slot], lore);
    }

}
