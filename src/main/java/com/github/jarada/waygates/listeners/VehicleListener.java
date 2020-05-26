package com.github.jarada.waygates.listeners;

import com.github.jarada.waygates.WaygateManager;
import com.github.jarada.waygates.data.BlockLocation;
import com.github.jarada.waygates.data.DataManager;
import com.github.jarada.waygates.data.Gate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.List;

public class VehicleListener implements Listener {

    private final DataManager dm;
    private final WaygateManager gm;

    public VehicleListener() {
        dm = DataManager.getManager();
        gm = WaygateManager.getManager();
    }

    /* Vehicle Movement */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent e) {
        // Verify Movement
        BlockLocation from = new BlockLocation(e.getFrom());
        BlockLocation to = new BlockLocation(e.getTo());
        if (from.equals(to))
            return;

        // Verify Gate
        Gate gate = gm.getGateAtLocation(to);
        if (gate == null)
            return;

        // Verify Passengers
        Vehicle vehicle = e.getVehicle();
        List<Entity> passengers = vehicle.getPassengers();

        // Needs to check that A) there are Players, and B) ALL Players verify
        boolean verified = true;
        boolean hasPlayer = false;
        for (Entity passenger : passengers) {
            if (passenger instanceof Player) {
                hasPlayer = true;
                verified = verified && gate.verify((Player) passenger);
            }
        }
        if (!hasPlayer || !verified)
            return;

        // Transport!
        gate.teleportVehicle(vehicle);
        dm.saveWaygate(gate, false);
    }

}
