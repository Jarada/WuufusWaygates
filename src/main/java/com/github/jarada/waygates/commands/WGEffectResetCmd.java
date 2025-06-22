package com.github.jarada.waygates.commands;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.GateActivationEffect;
import com.github.jarada.waygates.data.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class WGEffectResetCmd implements PluginCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        String worldName = null;
        if (args.length >= 1) {
            worldName = args[0];
        }

        // World Name?
        if (worldName == null) {
            Msg.EFFECT_RESET_WORLD_NOT_GIVEN.sendTo(sender);
            return;
        }

        // See if we have gates in that world
        WaygateManager wm = WaygateManager.getManager();
        List<Gate> gates = wm.getAllGatesInWorld(worldName, true);
        if (gates.isEmpty()) {
            Msg.EFFECT_RESET_WORLD_EMPTY.sendTo(sender);
            return;
        }

        // OK, let's go
        for (Gate gate : gates) {
            if (gate.getActivationEffect() != GateActivationEffect.NETHER) {
                gate.resetActivationEffect();
                DataManager.getManager().saveWaygate(gate, false);
            }
        }
        Msg.EFFECT_RESET_WORLD_COMPLETED.sendTo(sender, worldName);
    }

    @Override
    public boolean isConsoleExecutable() {
        return true;
    }

    @Override
    public boolean hasRequiredPerm(CommandSender sender) {
        return sender.hasPermission("wg.command.world.reset");
    }

}
