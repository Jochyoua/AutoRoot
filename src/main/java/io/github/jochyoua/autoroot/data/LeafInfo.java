package io.github.jochyoua.autoroot.data;

import lombok.Getter;
import lombok.ToString;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

@Getter
@ToString
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

}