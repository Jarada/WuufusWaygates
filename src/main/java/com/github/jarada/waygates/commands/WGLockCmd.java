package com.github.jarada.waygates.commands;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.BlockLocation;
import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class WGLockCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        DataManager dm = DataManager.getManager();
        WaygateManager wm = WaygateManager.getManager();
        Player p = (Player) sender;

        // Get Gate Near Location
        List<Gate> gates = wm.getGatesNearLocation(new BlockLocation(p.getLocation()));
        if (gates.size() == 1) {
            // Add Lock to Inventory
            ItemStack constructor = dm.getLockForGate(gates.get(0));
            p.getInventory().addItem(constructor);
        } else {
            Msg.CMD_INVALID_GATES.sendTo(p, gates.size());
        }
    }

    @Override
    public boolean isConsoleExecutable() {
        return false;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wg.command.item.lock");
    }

}
