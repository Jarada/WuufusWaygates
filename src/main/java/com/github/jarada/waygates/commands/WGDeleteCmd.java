package com.github.jarada.waygates.commands;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class WGDeleteCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        String worldName = null;
        boolean confirm = false;
        if (args.length >= 1) {
            worldName = args[0];
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("confirm")) {
            confirm = true;
        }

        // World Name?
        if (worldName == null) {
            Msg.DELETE_WORLD_NOT_GIVEN.sendTo(sender);
            return;
        }

        // See if we have gates in that world
        WaygateManager wm = WaygateManager.getManager();
        List<Gate> gates = wm.getAllGatesInWorld(worldName, true);
        if (gates.isEmpty()) {
            Msg.DELETE_WORLD_EMPTY.sendTo(sender);
            return;
        }

        // OK, let's go
        final String actualWorldName = worldName;
        if (confirm && wm.isWorldAwaitingDeletion(worldName)) {
            Msg.DELETE_WORLD_ACTION.sendTo(sender, gates.size(), actualWorldName);
            Bukkit.getScheduler().runTaskAsynchronously(PluginMain.getPluginInstance(), () -> {
                for (Gate gate : gates) {
                    wm.destroyWaygate(gate);
                }
                Msg.DELETE_WORLD_COMPLETED.sendTo(sender, actualWorldName);
            });
        } else {
            Msg.DELETE_WORLD_WARNING.sendTo(sender, gates.size(), actualWorldName, actualWorldName);
            wm.setWorldForDeletion(worldName);
            Bukkit.getScheduler().runTaskLater(PluginMain.getPluginInstance(), () -> wm.clearWorldForDeletion(actualWorldName), 400L);
        }
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wg.command.world.delete");
    }
}
