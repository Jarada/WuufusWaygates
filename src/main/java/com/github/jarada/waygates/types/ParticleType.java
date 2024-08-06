package com.github.jarada.waygates.types;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ParticleType {

    ENCHANTMENT {
        @Override
        public Particle get() {
            return retrieve("ENCHANTMENT_TABLE", "ENCHANT");
        }
    },
    EXPLOSION {
        @Override
        public Particle get() {
            return retrieve("EXPLOSION_LARGE", "EXPLOSION");
        }
    },
    PORTAL {
        @Override
        public Particle get() {
            return Particle.PORTAL;
        }
    },
    REDSTONE {
        @Override
        public Particle get() {
            return retrieve("REDSTONE", "DUST");
        }
    },
    SPELL {
        @Override
        public Particle get() {
            return retrieve("SPELL_MOB_AMBIENT", "OMINOUS_SPAWNING");
        }
    },
    WATER {
        @Override
        public Particle get() {
            return retrieve("SPELL_MOB", "ENTITY_EFFECT");
        }

        @Override
        protected Color getColor() {
            return Color.NAVY;
        }
    };

    public abstract Particle get();

    protected Color getColor() {
        return null;
    }

    public <T> void spawn(@NotNull World world, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {
        spawn(world, location, count, offsetX, offsetY, offsetZ, extra, getColor());
    }

    protected <T> void spawn(@NotNull World world, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data) {
        if (getColor() != null) {
            try {
                world.spawnParticle(get(), location, count, offsetX, offsetY, offsetZ, extra, data);
                return;
            } catch (IllegalArgumentException e) {
                // 1.20- Support
            }
        }
        world.spawnParticle(get(), location, count, offsetX, offsetY, offsetZ, extra);
    }

    protected Particle retrieve(String oldValue, String newValue) {
        try {
            // 1.20- Support
            return Particle.valueOf(oldValue);
        } catch (IllegalArgumentException e) {
            return Particle.valueOf(newValue);
        }
    }
}
