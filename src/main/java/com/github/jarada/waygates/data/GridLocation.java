package com.github.jarada.waygates.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class GridLocation extends BlockLocation {

    private float             pitch, yaw;

    public GridLocation(Location loc) {
        super(loc);
        setLocation(loc);
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(getWorldName()), getX(), getY(), getZ(), yaw, pitch);
    }

    public void setLocation(Location loc) {
        super.setLocation(loc);
        pitch = loc.getPitch();
        yaw = loc.getYaw();
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = super.hashCode() + (int)pitch + (int)yaw;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GridLocation) {
            GridLocation incoming = (GridLocation)obj;
            return super.equals(obj) &&
                    this.pitch == incoming.pitch &&
                    this.yaw == incoming.yaw;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("GridLocation(%d.%d.%d %f %f %s)",
                this.getX(), this.getY(), this.getZ(),
                this.pitch, this.yaw, this.getWorldName());
    }
}
