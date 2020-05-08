package com.github.jarada.waygates.types;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.*;

public enum GateOrientation {

    NS(new ArrayList<>(Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN))),
    WE(new ArrayList<>(Arrays.asList(BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN)));

    private final Set<BlockFace> expandFaces;

    GateOrientation(Collection<BlockFace> expandFaces)
    {
        java.util.Set<BlockFace> expandFacesTemp = new HashSet<>(expandFaces);
        this.expandFaces = Collections.unmodifiableSet(expandFacesTemp);
    }

    public Set<BlockFace> getExpandFaces() {
        return expandFaces;
    }

    public BlockFace getExitFace(Location exit, Location gate)
    {
        int mod;
        if (this == NS)
        {
            mod = exit.getBlockX() - gate.getBlockX();
            if (mod > 0)
            {
                return BlockFace.WEST;
            }
            else
            {
                return BlockFace.EAST;
            }
        }
        else
        {
            mod = exit.getBlockZ() - gate.getBlockZ();
            if (mod > 0)
            {
                return BlockFace.NORTH;
            }
            else
            {
                return BlockFace.SOUTH;
            }
        }
    }

    public float getExitYaw(Location exit, Location gate)
    {
        return getYaw(getExitFace(exit, gate));
    }

    public static Float getYaw(BlockFace face)
    {
        switch (face)
        {
            case NORTH: return 0f;
            case EAST: return 90f;
            case SOUTH: return 180f;
            case WEST: return 270f;
            case NORTH_EAST: return 45f;
            case NORTH_WEST: return 315f;
            case SOUTH_EAST: return 135f;
            case SOUTH_WEST: return 225f;
            case WEST_NORTH_WEST: return 292.5f;
            case NORTH_NORTH_WEST: return 337.5f;
            case NORTH_NORTH_EAST: return 22.5f;
            case EAST_NORTH_EAST: return 67.5f;
            case EAST_SOUTH_EAST: return 112.5f;
            case SOUTH_SOUTH_EAST: return 157.5f;
            case SOUTH_SOUTH_WEST: return 202.5f;
            case WEST_SOUTH_WEST: return 247.5f;
            case UP:
            case DOWN:
            case SELF:
                return null;
        }
        return null;
    }

}
