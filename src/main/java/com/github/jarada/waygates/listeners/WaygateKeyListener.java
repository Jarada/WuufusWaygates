package com.github.jarada.waygates.listeners;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.BlockLocation;
import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.Msg;
import com.github.jarada.waygates.events.WaygateKeyUseEvent;
import com.github.jarada.waygates.menus.MenuManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public class WaygateKeyListener implements Listener {

    private static WaygateKeyListener listener;

    public static WaygateKeyListener getListener() {
        if (listener == null)
            listener = new WaygateKeyListener();

        return listener;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onWaygateKeyUse(WaygateKeyUseEvent useEvent) {
        Player p = useEvent.getPlayer();
        Action a = useEvent.getAction();
        Block b = useEvent.getClickedBlock();

        Gate gate = WaygateManager.getManager().getGateAtLocation(new BlockLocation(b.getLocation()));
        if (gate != null) {
            if (p.hasPermission("wg.key.use") && (a == Action.RIGHT_CLICK_BLOCK || a == Action.RIGHT_CLICK_AIR)) {
                if (gate.isOwnerPrivate() && !(gate.getOwner().equals(p.getUniqueId()) || p.hasPermission("wg.bypass")))
                    Msg.GATE_ACCESS_DENIED.sendTo(p);
                else
                    new MenuManager(p, gate).openWaygateMenu();
            }
        }
    }

}
