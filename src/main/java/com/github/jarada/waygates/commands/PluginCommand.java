package com.github.jarada.waygates.commands;

import org.bukkit.command.CommandSender;

public interface PluginCommand {

    void execute(CommandSender sender, String[] args);

    boolean isConsoleExecutable();

    boolean hasRequiredPerm(CommandSender sender);

}
