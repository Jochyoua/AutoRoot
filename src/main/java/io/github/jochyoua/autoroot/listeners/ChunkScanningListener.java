package io.github.jochyoua.autoroot.listeners;

import io.github.jochyoua.autoroot.AutoRoot;
import io.github.jochyoua.autoroot.LeafInfo;
import io.github.jochyoua.autoroot.services.ConfigService;
import io.github.jochyoua.autoroot.tasks.NaturalSeedFallTask;
import lombok.Getter;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChunkScanningListener implements Listener {
    private final AutoRoot plugin;
    @Getter
    private final Set<Long> staleChunkCache = new HashSet<>();

    public ChunkScanningListener(AutoRoot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if(!plugin.isEnableFunctionality()) return;
        NaturalSeedFallTask task = plugin.getNaturalSeedFallTask();
        Chunk chunk = event.getChunk();

        if (!isChunkNearAnyPlayer(chunk)) {
            return;
        }

        long key = task.convertChunkKey(chunk.getX(), chunk.getZ());

        if (task.getLeafCache().containsKey(key) || staleChunkCache.contains(key)) return;

        List<LeafInfo> leaves = task.scanChunkLeaves(chunk);
        task.getLeafCache().put(key, leaves);
        staleChunkCache.add(key);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if(!plugin.isEnableFunctionality()) return;
        NaturalSeedFallTask task = plugin.getNaturalSeedFallTask();
        Chunk chunk = event.getChunk();
        long key = task.convertChunkKey(chunk.getX(), chunk.getZ());
        staleChunkCache.remove(key);
    }

    private boolean isChunkNearAnyPlayer(Chunk chunk) {
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        for (Player player : chunk.getWorld().getPlayers()) {
            Chunk pc = player.getLocation().getChunk();

            int px = pc.getX();
            int pz = pc.getZ();

            int dx = Math.abs(px - chunkX);
            int dz = Math.abs(pz - chunkZ);

            if (dx <= 10 && dz <= 10) {
                return true;
            }
        }

        return false;
    }
}
