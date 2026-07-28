package io.github.jochyoua.autoroot;

import io.github.jochyoua.autoroot.services.ConfigService;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@ToString
public final class PlantableRule {

    private final Material plantBlock;
    private final Set<Material> triggerItems;
    private final Set<Material> validBlocksBelow;
    private final double plantChance;
    private final Set<Biome> whitelistedBiomes;
    private final boolean enableFallingSeeds;
    private final Set<String> commandsToExecute;
    private final boolean destoryItemsOnFailure;
    private final Set<Particle> successParticles;
    private final Set<Particle> failureParticles;
    private final Set<Sound> successSounds;
    private final Set<Sound> failureSounds;
    private final Set<UUID> worldWhitelist;
    private final Set<UUID> worldBlacklist;

    public boolean isBiomeAllowed(Block soil) {
        Set<Biome> biomeSet = getWhitelistedBiomes();
        return biomeSet.isEmpty() || biomeSet.contains(soil.getBiome());
    }

    public boolean isAirOrVegetation(Block block, ConfigService config) {
        Material type = block.getType();
        if (type.isAir()) return true;
        return isVegetation(type, config);
    }

    public boolean isVegetation(Material type, ConfigService config) {
        if (config.getIgnoredVegetationBlocks().contains(type)) {
            return true;
        }
        for (Tag<Material> tag : config.getIgnoredVegetationTags()) {
            if (tag.isTagged(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean passesChance(double randomValue, double defaultChance) {
        double chance = plantChance >= 0 ? plantChance : defaultChance;
        return randomValue <= chance;
    }

    public boolean isWorldBlacklisted(World world) {
        if (world == null) return false;

        return worldBlacklist.contains(world.getUID());
    }

    public boolean isWorldWhitelisted(World world) {
        if (world == null) return false;

        return worldWhitelist.contains(world.getUID());
    }

}

