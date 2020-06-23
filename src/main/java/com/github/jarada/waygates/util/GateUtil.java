package com.github.jarada.waygates.util;

import com.github.jarada.waygates.data.Gate;
import com.github.jarada.waygates.data.GridLocation;
import com.github.jarada.waygates.types.GateOrientation;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public class GateUtil {

    public static GridLocation getExitForGate(Location playerLoc, Location blockLoc, @Nullable Gate gate, @Nullable GateOrientation gateOrientation) {
        if (gateOrientation == null && gate != null) {
            Axis axis = gate.getOrientation();
            if (axis == Axis.X)
                gateOrientation = GateOrientation.WE;
            else
                gateOrientation = GateOrientation.NS;
        }
        if (gateOrientation != null) {
            playerLoc.setPitch(0F);
            playerLoc.setYaw(gateOrientation.getExitYaw(playerLoc, blockLoc));
        }
        return new GridLocation(playerLoc);
    }

}
