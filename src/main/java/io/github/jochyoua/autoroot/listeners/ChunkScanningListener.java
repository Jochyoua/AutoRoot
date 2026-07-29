package io.github.jochyoua.autoroot.listeners;

import io.github.jochyoua.autoroot.AutoRoot;
import io.github.jochyoua.autoroot.data.ChunkInfo;
import io.github.jochyoua.autoroot.services.ConfigService;
import io.github.jochyoua.autoroot.tasks.NaturalSeedFallTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkScanningListener implements Listener {
    private final AutoRoot plugin;
    private final ConfigService config;
    @Getter
    private final Set<Long> staleChunkCache = ConcurrentHashMap.newKeySet();

    public ChunkScanningListener(AutoRoot plugin, ConfigService configService) {
        this.plugin = plugin;
        this.config = configService;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!plugin.isEnableFunctionality()) return;
        NaturalSeedFallTask task = plugin.getNaturalSeedFallTask();
        Chunk chunk = event.getChunk();

        if (!isChunkNearAnyPlayer(chunk)) {
            return;
        }

        long key = ChunkInfo.convertKey(chunk);

        if (staleChunkCache.contains(key)) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            if(!task.getChunkCache().containsKey(key)) {
                task.getChunkCache().put(key, task.scanChunk(chunk));
            }
        });
        staleChunkCache.add(key);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!plugin.isEnableFunctionality()) return;
        Chunk chunk = event.getChunk();
        long key = ChunkInfo.convertKey(chunk);
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
