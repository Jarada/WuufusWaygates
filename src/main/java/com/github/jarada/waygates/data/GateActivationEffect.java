package com.github.jarada.waygates.data;

import com.github.jarada.waygates.PluginMain;
import com.github.jarada.waygates.util.Util;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.Orientable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public enum GateActivationEffect {

    MAGIC {

        @Override
        public void activateGate(Gate gate) {
            activeGates.add(gate);
            Util.playSound(gate.getCenterBlock().getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE);
            if (gate.isInLoadedChunks()) {
                initialiseParticleEffects(gate);
            }
        }

        @Override
        public void loadChunk(Gate gate) {
            super.loadChunk(gate);
            if (activeGates.contains(gate)) {
                initialiseParticleEffects(gate);
            }
        }

        private void initialiseParticleEffects(Gate gate) {
            if (loadedGates.contains(gate)) return;
            loadedGates.add(gate);
            if (dataManager.WG_GATE_EFFECT_PARTICLES.getSize() > 0) {
                clearParticleEffects(gate);
                runnables.put(gate, Bukkit.getScheduler().runTaskTimer(PluginMain.getPluginInstance(), () -> {
                    try {
                        for (BlockLocation blockLocation : gate.getCoords()) {
                            Location adjustedLocation = blockLocation.getCentralLocation();
                            Objects.requireNonNull(blockLocation.getLocation().getWorld())
                                    .spawnParticle(Particle.ENCHANTMENT_TABLE, adjustedLocation, 2, getOffsetX(gate), offsetY, getOffsetZ(gate), 0.8);
                            blockLocation.getLocation().getWorld().spawnParticle(Particle.SPELL_MOB_AMBIENT, adjustedLocation, 1, getOffsetX(gate), offsetY, getOffsetZ(gate), 0.8);
                            blockLocation.getLocation().getWorld().spawnParticle(Particle.PORTAL, adjustedLocation, 2, getOffsetX(gate), offsetY, getOffsetZ(gate), 0.8);
                        }
                    } catch (NullPointerException e) {
                        // Pass
                    }
                }, 0, dataManager.WG_GATE_EFFECT_PARTICLES.getSize()));
            }
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
            if (gate.isInLoadedChunks()) {
                initialiseParticleEffects(gate);
            }
        }

        @Override
        public void loadChunk(Gate gate) {
            super.loadChunk(gate);
            if (activeGates.contains(gate)) {
                initialiseParticleEffects(gate);
            }
        }

        @Override
        protected boolean isBlockMaterialChangeable(Material material) {
            return Util.isMaterialAir(material) || material == Material.WATER;
        }

        private void initialiseParticleEffects(Gate gate) {
            if (loadedGates.contains(gate)) return;
            loadedGates.add(gate);
            if (dataManager.WG_GATE_EFFECT_PARTICLES.getSize() > 0) {
                clearParticleEffects(gate);
                runnables.put(gate, Bukkit.getScheduler().runTaskTimer(PluginMain.getPluginInstance(), () -> {
                    try {
                        for (BlockLocation blockLocation : gate.getCoords()) {
                            Location adjustedLocation = blockLocation.getCentralLocation();
                            Objects.requireNonNull(blockLocation.getLocation().getWorld()).spawnParticle(Particle.SPELL_MOB, adjustedLocation, 0, 0.24, 0.27, 0.66, 1.0);
                        }
                    } catch (NullPointerException e) {
                        // Pass
                    }
                }, 0, dataManager.WG_GATE_EFFECT_PARTICLES.getSize()));
            }
        }

        @Override
        public String toString() {
            return Msg.MENU_TEXT_ACTIVATION_EFFECT_WATER.toString();
        }
    };

    protected static final List<Gate> activeGates = new ArrayList<>();
    protected static final List<Gate> loadedGates = new ArrayList<>();
    protected static final Map<Gate, BukkitTask> runnables = new HashMap<>();

    private static final GateActivationEffect[] vals = values();
    private static final DataManager dataManager = DataManager.getManager();
    private static final double offsetY = 0.075;

    public GateActivationEffect next()
    {
        return vals[(this.ordinal()+1) % vals.length];
    }

    public void activateGate(Gate gate) {
        setContent(gate, Material.AIR);
    }

    public void deactivateGate(Gate gate) {
        setContent(gate, Material.AIR);
        activeGates.remove(gate);
        loadedGates.remove(gate);
        clearParticleEffects(gate);
    }

    public void loadChunk(Gate gate) {
        // To Be Overridden
    }

    public void unloadChunk(Gate gate) {
        loadedGates.remove(gate);
        clearParticleEffects(gate);
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

    protected double getOffsetZ(Gate gate) {
        Axis axis = gate.getOrientation();
        return (Axis.Z.equals(axis)) ? 0.075 : 0.75;
    }

    protected void clearParticleEffects(Gate gate) {
        if (runnables.containsKey(gate) && !runnables.get(gate).isCancelled())
            runnables.get(gate).cancel();
    }

}
