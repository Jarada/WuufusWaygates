package com.github.jarada.waygates.data;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.util.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public enum GateActivationEffect {

    MAGIC {

        @Override
        public void activateGate(Gate gate) {
            activeGates.add(gate);
            Util.playSound(gate.getCenterBlock().getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE);
            if (gate.getActiveLocation().getLocation().getChunk().isLoaded()) {
                initialiseParticleEffects(gate);
            }
        }

        @Override
        public void deactivateGate(Gate gate) {
            super.deactivateGate(gate);
            activeGates.remove(gate);
            loadedGates.remove(gate);
        }

        @Override
        public void loadChunk(Gate gate) {
            super.loadChunk(gate);
            if (activeGates.contains(gate)) {
                initialiseParticleEffects(gate);
            }
        }

        @Override
        public void unloadChunk(Gate gate) {
            super.unloadChunk(gate);
            loadedGates.remove(gate);
        }

        private void initialiseParticleEffects(Gate gate) {
            loadedGates.add(gate);
            new BukkitRunnable() {
                @Override
                public void run() {

                    if (!loadedGates.contains(gate)) {
                        cancel();
                        return;
                    }

                    for (BlockLocation blockLocation : gate.getCoords()) {
                        Location adjustedLocation = blockLocation.getCentralLocation();
                        blockLocation.getLocation().getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, adjustedLocation, 2, getOffsetX(gate), getOffsetY(gate), getOffsetZ(gate), 0.8);
                        blockLocation.getLocation().getWorld().spawnParticle(Particle.SPELL, adjustedLocation, 1, getOffsetX(gate), getOffsetY(gate), getOffsetZ(gate), 0.8);
                        blockLocation.getLocation().getWorld().spawnParticle(Particle.PORTAL, adjustedLocation, 2, getOffsetX(gate), getOffsetY(gate), getOffsetZ(gate), 0.8);
                    }
                }
                
            }.runTaskTimer(PluginMain.getPluginInstance(), 10, 10);
        }

        @Override
        public String toString() {
            return Msg.MENU_TEXT_ACTIVATION_EFFECT_MAGIC.toString();
        }
    },
    NETHER {
        @Override
        public void activateGate(Gate gate) {
            setContent(gate, Material.NETHER_PORTAL);
        }

        @Override
        protected boolean isBlockMaterialChangeable(Material material) {
            return Util.isMaterialAir(material) || material == Material.NETHER_PORTAL;
        }

        @Override
        public String toString() {
            return Msg.MENU_TEXT_ACTIVATION_EFFECT_NETHER.toString();
        }
    },
    WATER {
        @Override
        public void activateGate(Gate gate) {
            activeGates.add(gate);
            setContent(gate, Material.WATER);
            Util.playSound(gate.getCenterBlock().getLocation(), Sound.AMBIENT_UNDERWATER_ENTER);
            if (gate.getActiveLocation().getLocation().getChunk().isLoaded()) {
                initialiseParticleEffects(gate);
            }
        }

        @Override
        public void deactivateGate(Gate gate) {
            super.deactivateGate(gate);
            activeGates.remove(gate);
            loadedGates.remove(gate);
        }

        @Override
        public void loadChunk(Gate gate) {
            super.loadChunk(gate);
            if (activeGates.contains(gate)) {
                initialiseParticleEffects(gate);
            }
        }

        @Override
        public void unloadChunk(Gate gate) {
            super.unloadChunk(gate);
            loadedGates.remove(gate);
        }

        @Override
        protected boolean isBlockMaterialChangeable(Material material) {
            return Util.isMaterialAir(material) || material == Material.WATER;
        }

        private void initialiseParticleEffects(Gate gate) {
            loadedGates.add(gate);
            new BukkitRunnable() {
                @Override
                public void run() {

                    if (!loadedGates.contains(gate)) {
                        cancel();
                        return;
                    }
                    
                    for (BlockLocation blockLocation : gate.getCoords()) {
                        Location adjustedLocation = blockLocation.getCentralLocation();
                        blockLocation.getLocation().getWorld().spawnParticle(Particle.SPELL_MOB, adjustedLocation, 0, 0.24, 0.27, 0.66, 1.0);
                    }
                }

            }.runTaskTimer(PluginMain.getPluginInstance(), 10, 10);
        }

        @Override
        public String toString() {
            return Msg.MENU_TEXT_ACTIVATION_EFFECT_WATER.toString();
        }
    };

    protected List<Gate> activeGates = new ArrayList<>();
    protected List<Gate> loadedGates = new ArrayList<>();

    private static GateActivationEffect[] vals = values();

    public GateActivationEffect next()
    {
        return vals[(this.ordinal()+1) % vals.length];
    }

    public void activateGate(Gate gate) {
        setContent(gate, Material.AIR);
    }

    public void deactivateGate(Gate gate) {
        setContent(gate, Material.AIR);
    }

    public void loadChunk(Gate gate) {

    }

    public void unloadChunk(Gate gate) {

    }

    public void setContent(@NotNull Gate gate, Material material)
    {
        List<Block> blocks = gate.getBlocks();
        if (blocks == null) return;

        // Orientation check
        Axis axis = gate.getOrientation();

        // Set Content
        for (Block block : blocks)
        {
            Material blockMaterial = block.getType();
            
            if (!isBlockMaterialChangeable(blockMaterial)) continue;

            block.setType(material, Util.isMaterialAir(blockMaterial));

            // Apply orientation
            if (material != Material.NETHER_PORTAL && !MultipleFacing.class.isAssignableFrom(material.data)) continue;

            BlockData data = block.getBlockData();
            if (data instanceof Orientable) {
                ((Orientable) data).setAxis(axis);
                block.setBlockData(data);
            }
        }
    }

    public void playTeleportSound(Location activeLocation) {
        Util.playSound(activeLocation, Sound.ENTITY_GHAST_SHOOT);
    }

    protected boolean isBlockMaterialChangeable(Material material) {
        return Util.isMaterialAir(material);
    }

    protected double getOffsetX(Gate gate) {
        Axis axis = gate.getOrientation();
        return (Axis.X.equals(axis)) ? 0.075 : 0.75;
    }

    protected double getOffsetY(Gate gate) {
        return 0.075;
    }

    protected double getOffsetZ(Gate gate) {
        Axis axis = gate.getOrientation();
        return (Axis.Z.equals(axis)) ? 0.075 : 0.75;
    }

}
