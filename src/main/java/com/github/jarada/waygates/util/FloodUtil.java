package com.github.jarada.waygates.util;

import com.github.jarada.waygates.types.GateOrientation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FloodUtil {

    public static Map.Entry<GateOrientation, Set<Block>> getFloodInfo(Block startBlock, int maxarea) {
        GateOrientation gateOrientation;
        Set<Block> blocksNS = getFloodBlocks(startBlock, new HashSet<>(), GateOrientation.NS.getExpandFaces(), maxarea);
        Set<Block> blocksWE = getFloodBlocks(startBlock, new HashSet<>(), GateOrientation.WE.getExpandFaces(), maxarea);

        // OK, what direction are we facing?
        Set<Block> blocks;
        if (blocksNS != null && blocksWE != null) {
            if (blocksNS.size() > blocksWE.size())
            {
                blocks = blocksWE;
                gateOrientation = GateOrientation.WE;
            }
            else
            {
                blocks = blocksNS;
                gateOrientation = GateOrientation.NS;
            }
        } else if (blocksNS != null) {
            blocks = blocksNS;
            gateOrientation = GateOrientation.NS;
        } else if (blocksWE != null) {
            blocks = blocksWE;
            gateOrientation = GateOrientation.WE;
        } else {
            return null;
        }

        // Add the Frame too
        blocks = expandedByOne(blocks, gateOrientation.getExpandFaces());

        return new AbstractMap.SimpleEntry<>(gateOrientation, blocks);
    }

    public static Set<Block> getFloodBlocks(Block startBlock, Set<Block> foundBlocks, Set<BlockFace> expandFaces, int maxarea) {
        if (foundBlocks == null)
        {
            return null;
        }

        if  (foundBlocks.size() > maxarea)
        {
            return null;
        }

        if (foundBlocks.contains(startBlock))
        {
            return foundBlocks;
        }

        if (Util.isMaterialAir(startBlock.getType()))
        {
            // We have an air/portal block, let's add to our internals
            foundBlocks.add(startBlock);

            // More! More! Get More!
            for (BlockFace face : expandFaces)
            {
                Block potentialBlock = startBlock.getRelative(face);
                foundBlocks = getFloodBlocks(potentialBlock, foundBlocks, expandFaces, maxarea);
            }
        }

        return foundBlocks;
    }

    public static Set<Block> expandedByOne(Set<Block> blocks, Set<BlockFace> expandFaces)
    {
        Set<Block> ret = new HashSet<>(blocks);

        for (Block block : blocks)
        {
            for (BlockFace face : expandFaces)
            {
                Block potentialBlock = block.getRelative(face);
                if (ret.contains(potentialBlock)) continue;
                ret.add(potentialBlock);
            }
        }

        return ret;
    }

}
