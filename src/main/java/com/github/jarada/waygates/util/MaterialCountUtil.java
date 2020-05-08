package com.github.jarada.waygates.util;

import com.github.jarada.waygates.data.Msg;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;
import java.util.Map.Entry;

public class MaterialCountUtil
{
    public static Map<Material, Integer> count(Collection<Block> blocks)
    {
        Map<Material, Integer> ret = new HashMap<>();
        for (Block block : blocks)
        {
            Material material = block.getType();
            if ( ! ret.containsKey(material))
            {
                ret.put(material, 1);
                continue;
            }
            ret.put(material, ret.get(material)+1);
        }
        return ret;
    }

    public static boolean has(Map<Material, Integer> me, Map<Material, Integer> req)
    {
        for (Entry<Material, Integer> entry : req.entrySet())
        {
            Material material = entry.getKey();
            Integer reqCount = entry.getValue();
            Integer meCount = me.get(material);
            if (meCount == null || meCount < reqCount) return false;
        }
        return true;
    }

    public static String desc(List<Map<Material, Integer>> groupedMaterialCounts)
    {
        if (groupedMaterialCounts.size() > 5)
            return Msg.GATE_MUST_CONTAIN_GROUPED.toString();

        List<String> groupedParts = new ArrayList<>();
        for (Map<Material, Integer> materialCounts : groupedMaterialCounts) {
            List<String> parts = new ArrayList<>();
            for (Entry<Material, Integer> entry : materialCounts.entrySet()) {
                Material material = entry.getKey();
                Integer count = entry.getValue();
                String part = String.format("%d %s", count, material.toString());
                parts.add(part);
            }
            groupedParts.add(String.join(",", parts));
        }
        if (groupedMaterialCounts.size() > 1)
            return String.format("%s ", Msg.WORD_EITHER.toString()) +
                    String.join(String.format(" %s ", Msg.WORD_OR.toString()), groupedParts);
        return String.join(", ", groupedParts);
    }
}
