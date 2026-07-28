package io.github.jochyoua.autoroot.data;


import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Chunk;

import java.util.List;

@Builder
@ToString
@Getter
public final class ChunkInfo {
    private final long chunkKey;
    private final List<LeafInfo> leavesInChunk;
    private int plantedSaplings;
    public static long convertKey(Chunk chunk) {
        return (((long) chunk.getX()) << 32) ^ (chunk.getZ() & 0xffffffffL);
    }
    public void incrementSaplings(){
        this.plantedSaplings++;
    }
}
