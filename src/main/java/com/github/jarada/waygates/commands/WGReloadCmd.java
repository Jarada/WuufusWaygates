package com.github.jarada.waygates.commands;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import org.bukkit.command.CommandSender;

public class WGReloadCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        DataManager.getManager().reload();

        for (Gate gate : WaygateManager.getManager().getAllGates()) {
            if (!gate.isAlwaysOn()) {
                gate.deactivate();
            } else {
                gate.reopen();
            }
        }

        Msg.RELOADED.sendTo(sender);
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wg.command.reload");
    }

}
