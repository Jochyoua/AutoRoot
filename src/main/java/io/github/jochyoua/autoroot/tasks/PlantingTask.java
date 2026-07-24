package io.github.jochyoua.autoroot.tasks;

import io.github.jochyoua.autoroot.AutoRoot;
import io.github.jochyoua.autoroot.services.PlantingService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PlantingTask implements Runnable {
    private final AutoRoot plugin;
    private final PlantingService plantingService;

    public PlantingTask(AutoRoot plugin, PlantingService plantingService) {
        this.plugin = plugin;
        this.plantingService = plantingService;
    }

    @Override
    public void run() {
        if(!plugin.isEnableFunctionality()) return;
        for (World world : Bukkit.getWorlds()) {
            if (world.getPlayers().isEmpty()) continue;
            for (Item item : world.getEntitiesByClass(Item.class)) {
                Block blockBelow = item.getLocation().clone().subtract(0, 0.1, 0).getBlock();
                if (!blockBelow.getType().isSolid()) continue;
                PersistentDataContainer pdc = item.getPersistentDataContainer();
                if (pdc.has(plugin.alreadyScannedKey, PersistentDataType.BYTE)) {
                    continue;
                }
                pdc.set(plugin.alreadyScannedKey, PersistentDataType.BYTE, (byte) 1);
                plantingService.queuePlantingAttempt(item);
            }
        }
    }
}
