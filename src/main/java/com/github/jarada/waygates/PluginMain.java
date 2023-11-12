package com.github.jarada.waygates;

import com.github.jarada.waygates.commands.*;
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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
        commands.put("constructor", new WGConstructorCmd());
        commands.put("delete", new WGDeleteCmd());
        commands.put("key", new WGKeyCmd());
        commands.put("list", new WGListCmd());
        commands.put("lock", new WGLockCmd());
        commands.put("reload", new WGReloadCmd());

        try {
            Objects.requireNonNull(getCommand("wg")).setExecutor(this);
        } catch (NullPointerException ignored) {
            getLogger().warning("Unable to register commands; no commands available!");
        }
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
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, @NotNull String[] args) {
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
