package com.github.jarada.waygates;

import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.listeners.WaygateListener;
import com.github.jarada.waygates.listeners.PlayerListener;
import com.github.jarada.waygates.listeners.WaygateKeyListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

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
        DataManager.getManager().loadConfig();
        DataManager.getManager().loadWaygates();

        // TODO Commands - reload, list in world, delete from world

        getServer().getPluginManager().registerEvents(new WaygateKeyListener(), this);
        getServer().getPluginManager().registerEvents(new WaygateListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
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
        return false;
    }

}
