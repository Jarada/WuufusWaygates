package com.github.jarada.waygates;

import com.github.jarada.waygates.commands.PluginCommand;
import com.github.jarada.waygates.commands.WGDeleteCmd;
import com.github.jarada.waygates.commands.WGListCmd;
import com.github.jarada.waygates.commands.WGReloadCmd;
import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.listeners.VehicleListener;
import com.github.jarada.waygates.listeners.WaygateListener;
import com.github.jarada.waygates.listeners.PlayerListener;
import com.github.jarada.waygates.listeners.WaygateKeyListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PluginMain extends JavaPlugin {

    private static PluginMain          instance;
    private Map<String, PluginCommand> commands;

    public static PluginMain getPluginInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveResource("CHANGELOG.txt", true);
        DataManager.getManager().loadConfig(false);
        DataManager.getManager().loadWaygates();

        commands = new HashMap<>();
        commands.put("delete", new WGDeleteCmd());
        commands.put("list", new WGListCmd());
        commands.put("reload", new WGReloadCmd());

        getCommand("wg").setExecutor(this);
        getServer().getPluginManager().registerEvents(new WaygateKeyListener(), this);
        getServer().getPluginManager().registerEvents(new WaygateListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new VehicleListener(), this);
        getLogger().info("Waygate system online!");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
        instance = null;
        commands = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        PluginCommand pluginCmd = null;
        String[] param = null;

        if (cmd.equals("wg")) {
            if (args.length > 0) {
                String key = args[0].toLowerCase();
                param = Arrays.copyOfRange(args, 1, args.length);

                if (commands.containsKey(key))
                    pluginCmd = commands.get(key);
            }

            if (pluginCmd == null) {
                sender.sendMessage("Wuufu's Waygates v" + getDescription().getVersion() + " by Wuufu.");
                return false;
            }
        }

        assert pluginCmd != null;
        if (!(sender instanceof Player) && !pluginCmd.isConsoleExecutable()) {
            Msg.CMD_NO_CONSOLE.sendTo(sender);
            return true;
        }

        if (!pluginCmd.hasRequiredPerm(sender)) {
            Msg.NO_PERMS.sendTo(sender);
            return true;
        }

        pluginCmd.execute(sender, param);
        return true;
    }

}
