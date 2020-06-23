package com.github.jarada.waygates.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class BlockLocation {

    private String          worldName;
    private int               x, y, z;

    public BlockLocation(Location loc) {
        setLocation(loc);
    }

    public BlockLocation(String worldName, int x, int y, int z) {
        setWorldName(worldName);
        setX(x);
        setY(y);
        setZ(z);
    }

    public void setLocation(Location loc) {
        setWorld(loc.getWorld());
        setX(loc.getBlockX());
        setY(loc.getBlockY());
        setZ(loc.getBlockZ());
    }

    public Location getLocation() {
        return new Location(getWorld(), getX(), getY(), getZ());
    }

    public Location getTeleportLocation() {
        return getLocation().add(0.5, 0, 0.5);
    }

    public World getWorld() {
        if (worldName != null)
            return Bukkit.getWorld(worldName);
        return null;
    }

    public void setWorld(World world) {
        if (world != null)
            this.worldName = world.getName();
    }

    public String getWorldName() { return worldName; }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public int hashCode() {
        int result = x + y + z;
        result += (worldName != null) ? worldName.hashCode() : 0;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockLocation) {
            BlockLocation incoming = (BlockLocation)obj;
            return (this.worldName == null) ? incoming.worldName == null :
                    this.worldName.equals(incoming.worldName) &&
                    this.x == incoming.x &&
                    this.y == incoming.y &&
                    this.z == incoming.z;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("BlockLocation(%d.%d.%d %s)",
                this.x, this.y, this.z, this.worldName);
    }
}
