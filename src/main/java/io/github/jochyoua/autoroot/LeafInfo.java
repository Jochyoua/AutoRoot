package io.github.jochyoua.autoroot;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

@Getter
public class LeafInfo {
    private final Block block;
    private final Material material;
    private final boolean persistent;
    private final int distance;
    private final Biome biome;

    public LeafInfo(Block block, Material material, boolean persistent, int distance, Biome biome) {
        this.block = block;
        this.material = material;
        this.persistent = persistent;
        this.distance = distance;
        this.biome = biome;
    }


    @Override
    public String toString() {
        return "LeafInfo{" +
                "loc=" + (block != null ? block.getLocation() : "null") +
                ", material=" + material +
                ", persistent=" + persistent +
                ", distance=" + distance +
                ", biome=" + biome +
                '}';
    }
}
