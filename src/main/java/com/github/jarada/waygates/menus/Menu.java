package com.github.jarada.waygates.menus;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.data.Network;
import com.github.jarada.waygates.util.ItemStackUtil;
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
        this.currentWaygate.addActiveMenu(this);
    }

    void setup() {
        page = 1;
        buildMenu();
    }

    protected void destroy() {
        currentWaygate.removeActiveMenu(this);
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
        if (!ItemStackUtil.equals(clickEvent.getInventory().getContents(), optionIcons)
                || p != clickEvent.getWhoClicked())
            return false;

        if (!clickEvent.isCancelled())
            clickEvent.setCancelled(true);

        if (!clickEvent.isLeftClick())
            return false;

        final int slot = clickEvent.getRawSlot();

        return slot >= 0 && slot < size && optionNames[slot] != null;
    }

    @SuppressWarnings("unused")
    void onInventoryClose(InventoryCloseEvent closeEvent) {
        Bukkit.getScheduler().runTask(pm, this::destroy);
    }

    public void close() {
        if (ItemStackUtil.equals(p.getInventory().getContents(), optionIcons)) {
            p.closeInventory();
        }
    }

    public abstract void buildMenu();

    void initMenu() {
        size = 18;
        optionNames = new String[size];
        optionIcons = new ItemStack[size];
    }

    void addItemToMenu(int slot, Material icon, String name, String optionName) {
        addItemToMenu(slot, icon, name, optionName, null);
    }

    void addItemToMenu(int slot, Material icon, String name, String optionName, List<String> lore) {
        addItemToMenu(slot, icon, 1, name, optionName, lore);
    }

    void addItemToMenu(int slot, Material icon, int amount, String name, String optionName, List<String> lore) {
        ItemStack is = new ItemStack(icon, amount);
        Util.setItemNameAndLore(is, Util.color(name), lore);
        setOption(slot, optionName, is);
    }

    void addGateOwnerToMenu(boolean editable) {
        if (currentWaygate.getOwner() != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(currentWaygate.getOwner());
            List<String> lore = new ArrayList<>();
            lore.add(Util.color(editable ? Msg.MENU_TEXT_EDITABLE.toString(owner.getName()) : Msg.MENU_TEXT_STANDARD.toString(owner.getName())));
            ItemStack is = Util.getHead(owner, Util.color(Msg.MENU_TITLE_GATE_OWNER.toString()), lore);
            setOption(9, "Owner", is);
        }
    }

    void addNetworkOwnerToMenu(int slot, boolean editable) {
        if (currentWaygate.getNetwork().getOwner() != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(currentWaygate.getNetwork().getOwner());
            List<String> lore = new ArrayList<>();
            lore.add(Util.color(editable ? Msg.MENU_TEXT_EDITABLE.toString(owner.getName()) : Msg.MENU_TEXT_STANDARD.toString(owner.getName())));
            ItemStack is = Util.getHead(owner, Util.color(Msg.MENU_TITLE_NETWORK_OWNER.toString()), lore);
            setOption(slot, "Owner", is);
        }
    }

    void addNetworkToMenu(int slot, Network nw, boolean showSelected) {
        String networkName = (nw.isSystem()) ?
                Msg.MENU_COLOR_SYSTEM_NETWORK.toString() + Util.stripColor(nw.getName()) :
                Msg.MENU_COLOR_NETWORK.toString() + Util.stripColor(nw.getName());
        int gateCount = WaygateManager.getManager().countOfGatesInNetwork(p, nw, false);
        String displayName = networkName;

        List<String> lore = new ArrayList<>();
        if (!showSelected) {
            // Current Waygate Network Display
            displayName = Msg.MENU_TITLE_GATE_NETWORK.toString();
            lore.add(Util.color(networkName));
        }
        if (nw.isSystem()) {
            if (nw.equals(Network.getVoidNetwork()))
                lore.add(Util.color(Msg.NETWORK_SYSTEM_VOID.toString()));
            else
                lore.add(Util.color(Msg.NETWORK_SYSTEM.toString()));
        } else if (nw.isPrivate()) {
            lore.add(Util.color(Msg.NETWORK_PRIVATE.toString()));
        } else if (nw.isInvite()) {
            lore.add(Util.color(Msg.NETWORK_INVITE.toString()));
        } else if (nw.isFixed()) {
            lore.add(Util.color(Msg.NETWORK_FIXED.toString()));
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
        addItemToMenu(13, Material.BOOK, Math.min(page, 64), Msg.MENU_TITLE_PAGE.toString(), "Page", null);
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
        String gateName = Msg.MENU_COLOR_GATE.toString() + gate.getName();

        if (gate.isOwnerPrivate() && gate.isOwnerHidden())
            gateName += String.format(" %s", Msg.MENU_GATE_RESTRICTED.toString());
        else if (gate.isOwnerPrivate())
            gateName += String.format(" %s", Msg.MENU_GATE_PRIVATE.toString());
        else if (gate.isOwnerHidden())
            gateName += String.format(" %s", Msg.MENU_GATE_HIDDEN.toString());
        String displayName = gateName;

        List<String> lore = new ArrayList<>();
        if (gate == currentWaygate) {
            displayName = Msg.MENU_TITLE_GATE_INFO.toString();
            lore.add(Util.color(gateName));
        }
        if (loc.getWorld() != null)
            lore.add(Util.color(Msg.MENU_LORE_GATE_1.toString(loc.getWorld().getName())));
        else
            lore.add(Util.color(Msg.MENU_LORE_GATE_1.toString(Msg.MENU_TEXT_WORLD_NOT_FOUND.toString())));
        if (gate.getOwner() != null) {
            OfflinePlayer owner = Bukkit.getOfflinePlayer(gate.getOwner());
            lore.add(Util.color(Msg.MENU_LORE_GATE_2.toString(owner.getName())));
        }
        
        if (p.hasPermission("wg.see.cords") || sender.isOp()) 
            lore.add(Util.color(Msg.MENU_LORE_GATE_3.toString(loc.getBlockX())));
            lore.add(Util.color(Msg.MENU_LORE_GATE_4.toString(loc.getBlockY())));
            lore.add(Util.color(Msg.MENU_LORE_GATE_5.toString(loc.getBlockZ())));

        if (gate.getDescription() != null && gate.getDescription().length() > 0)
            lore.addAll(Arrays.asList(Util.getWrappedLore(gate.getDescription(), 25)));

        String colouredName = Util.color(displayName);
        ItemStack icon = new ItemStack(gate.getIcon(), 1);
        ItemStack itemLore = Util.setItemNameAndLore(icon, colouredName, lore);

        optionNames[slot] = colouredName;
        optionIcons[slot] = itemLore;
    }

}
