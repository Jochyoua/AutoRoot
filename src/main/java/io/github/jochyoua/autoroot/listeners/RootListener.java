package io.github.jochyoua.autoroot.listeners;

import io.github.jochyoua.autoroot.AutoRoot;
import io.github.jochyoua.autoroot.services.PlantingService;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class RootListener implements Listener {
    private final AutoRoot plugin;
    private final PlantingService plantingService;

    public RootListener(AutoRoot plugin, PlantingService plantingService) {
        this.plugin = plugin;
        this.plantingService = plantingService;
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if(!plugin.isEnableFunctionality()) return;
        Item item = event.getEntity();
        PersistentDataContainer pdc = item.getPersistentDataContainer();
        if (pdc.has(plugin.alreadyScannedKey, PersistentDataType.BYTE)) {
            return;
        }
        pdc.set(plugin.alreadyScannedKey, PersistentDataType.BYTE, (byte) 1);

        plantingService.queuePlantingAttempt(item);
    }

    @EventHandler
    public void onPlayerDropPlant(PlayerDropItemEvent event) {
        if(!plugin.isEnableFunctionality()) return;
        Item item = event.getItemDrop();
        PersistentDataContainer pdc = item.getPersistentDataContainer();
        if (pdc.has(plugin.alreadyScannedKey, PersistentDataType.BYTE)) {
            return;
        }
        pdc.set(plugin.alreadyScannedKey, PersistentDataType.BYTE, (byte) 1);
        plantingService.queuePlantingAttempt(item);
    }
}