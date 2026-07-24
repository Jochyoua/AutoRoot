package io.github.jochyoua.autoroot;

import io.github.jochyoua.autoroot.services.ConfigService;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.Set;

@Getter
@Builder
public final class PlantableRule {

    private final Material plantBlock;
    private final Set<Material> triggerItems;
    private final Set<Material> validBlocksBelow;
    private final double plantChance;
    private final Set<Biome> whitelistedBiomes;
    private final boolean enableFallingSeeds;
    private final Set<String> commandsToExecute;
    private final boolean destoryItemsOnFailure;

    @Override
    public String toString() {
        return "PlantableRule{" +
                "plantBlock=" + plantBlock +
                ", triggerItems=" + triggerItems +
                ", validBlocksBelow=" + validBlocksBelow +
                ", whitelistedBiomes=" + whitelistedBiomes +
                ", plantChance=" + plantChance +
                ", enableFallingSeeds=" + enableFallingSeeds +
                ", commandsToExecute=" + commandsToExecute +
                ", destroyItemsOnFailure=" + destoryItemsOnFailure +
                '}';
    }

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

}

