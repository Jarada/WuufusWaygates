package com.github.jarada.waygates.commands;

import com.github.jarada.waygates.data.CraftableWaygateItem;
import com.github.jarada.waygates.data.DataManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WGKeyCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        DataManager dm = DataManager.getManager();
        Player p = (Player) sender;

        ItemStack waygateKey = dm.getCraftableItemStack(CraftableWaygateItem.WAYGATE_KEY);
        p.getInventory().addItem(waygateKey);
    }

    @Override
    public boolean isConsoleExecutable() {
        return false;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wg.command.item.key");
    }

}
