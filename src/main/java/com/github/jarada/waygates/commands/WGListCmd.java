package com.github.jarada.waygates.commands;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class WGListCmd implements PluginCommand {

    private static int PAGE_SIZE = 5;

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Initialize parameters
        String worldName = null;
        int page = 1;

        // Do we have a world or a page?
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                worldName = args[0];
            }
        }

        // Do we have a world and a page?
        if (args.length > 1) {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1)
                    page = 1;
            } catch (NumberFormatException ignored) { }
        }

        // Get Gates
        List<Gate> gates;
        boolean accurate = true;
        if (worldName == null && sender instanceof Player) {
            // If no world is specified, the current world unless console
            Player player = (Player)sender;
            worldName = player.getWorld().getName();
        }
        if (worldName != null) {
            // Get a list of gates in the specified world
            gates = WaygateManager.getManager().getAllGatesInWorld(worldName, true);
            if (gates.size() == 0) {
                gates = WaygateManager.getManager().getAllGatesInWorld(worldName, false);
                accurate = false;
            }
        } else {
            // If still no world name specified, ALL WORLDS
            gates = WaygateManager.getManager().getAllGates();
        }

        if (gates.size() == 0) {
            if (worldName != null)
                Msg.LIST_NONE_FOUND_WORLD.sendTo(sender, worldName);
            else
                Msg.LIST_NONE_FOUND.sendTo(sender);
            return;
        }

        int pages = (int) Math.ceil((double)gates.size() / (double)PAGE_SIZE);
        if (page > pages)
            page = pages;
        if (worldName != null) {
            if (accurate)
                Msg.LIST_SUMMARY_WORLD.sendTo(sender, gates.size(), worldName, page, pages);
            else
                Msg.LIST_SUMMARY_WORLDS.sendTo(sender, gates.size(), worldName, page, pages);
        } else {
            Msg.LIST_SUMMARY.sendTo(sender, gates.size(), page, pages);
        }

        // Show Gates in Page
        for (int i = (page - 1) * PAGE_SIZE; i < page * PAGE_SIZE; i++) {
            try {
                Gate gate = gates.get(i);
                String owner = Bukkit.getOfflinePlayer(gate.getOwner()).getName();
                if (worldName != null && accurate)
                    Msg.LIST_GATE.sendTo(sender, gate.getName(), gate.getNetwork().getName(), owner,
                            gate.getExit().getX(), gate.getExit().getY(), gate.getExit().getZ());
                else
                    Msg.LIST_GATE_WORLDS.sendTo(sender, gate.getName(), gate.getNetwork().getName(), owner,
                            gate.getExit().getX(), gate.getExit().getY(), gate.getExit().getZ(), gate.getExit().getWorldName());
            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wg.command.world.list");
    }

}
