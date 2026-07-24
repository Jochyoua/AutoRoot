package io.github.jochyoua.autoroot;

import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import io.github.jochyoua.autoroot.commands.AutoRootCommand;
import io.github.jochyoua.autoroot.debug.DebugMessageHandler;
import io.github.jochyoua.autoroot.listeners.ChunkScanningListener;
import io.github.jochyoua.autoroot.listeners.RootListener;
import io.github.jochyoua.autoroot.services.ConfigService;
import io.github.jochyoua.autoroot.services.PlantingService;
import io.github.jochyoua.autoroot.tasks.NaturalSeedFallTask;
import io.github.jochyoua.autoroot.tasks.PlantingTask;
import lombok.Getter;
import lombok.Setter;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;


public final class AutoRoot extends JavaPlugin {

    public final NamespacedKey alreadyScannedKey = new NamespacedKey(this, "alreadyScanned");

    private ConfigService configService;
    @Getter
    private PlantingService plantingService;
    private RootListener rootListener;
    @Getter
    private ChunkScanningListener chunkScanningListener;
    @Getter
    private NaturalSeedFallTask naturalSeedFallTask;
    private DebugMessageHandler debugMessageHandler;
    @Setter
    @Getter
    private boolean enableFunctionality = true;
    @Getter
    UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        setupConfig();

        this.configService = new ConfigService(this);
        this.configService.reloadConfigValues();

        this.plantingService = new PlantingService(this, configService);

        if (configService.isUseEventListenersForItemDrops()) {
            this.rootListener = new RootListener(this, plantingService);
            getServer().getPluginManager().registerEvents(rootListener, this);
        } else {
            startPlantingTask();
        }

        this.chunkScanningListener = new ChunkScanningListener(this);
        getServer().getPluginManager().registerEvents(chunkScanningListener, this);

        startNaturalSeedFallTask();

        getLogger().info("AutoRoot enabled");

        PluginCommand pluginCommand = getCommand("autoroot");
        if (pluginCommand != null) {
            AutoRootCommand cmd = new AutoRootCommand(this, configService);
            pluginCommand.setExecutor(cmd);
            pluginCommand.setTabCompleter(cmd);
        }

        Metrics metrics = new Metrics(this, 32487);

        metrics.addCustomChart(new DrilldownPie("plantable_materials", () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();

            if (configService != null && configService.getPlantablesByBlock() != null) {
                configService.getPlantablesByBlock().forEach((material, rule) -> {
                    String materialName = material.name();
                    int chancePercent = (int) Math.round(rule.getPlantChance() * 100);
                    String chanceKey = chancePercent + "% chance";

                    Map<String, Integer> chanceMap = map.computeIfAbsent(materialName, k -> new HashMap<>());
                    chanceMap.put(chanceKey, 1);
                });
            }

            return map;
        }));

        debugMessageHandler = new DebugMessageHandler(this, configService.isEnableDebug());

        checkForUpdates();
    }


    public void debugMessage(String string) {
        if (debugMessageHandler != null) {
            debugMessageHandler.debugMessage(string);
        }
    }


    public CoreProtectAPI getCoreProtect() {
        Plugin coreProtect = Bukkit.getPluginManager().getPlugin("CoreProtect");
        if (!(coreProtect instanceof CoreProtect)) {
            return null;
        }

        CoreProtectAPI api = ((CoreProtect) coreProtect).getAPI();
        if (!api.isEnabled() || api.APIVersion() < 10) {
            return null;
        }

        return api;
    }

    public void startNaturalSeedFallTask() {
        if (naturalSeedFallTask != null) {
            naturalSeedFallTask.resetCaches();
        }
        int interval = configService.getFallingSeedsIntervalTicks();
        this.naturalSeedFallTask = new NaturalSeedFallTask(this, configService);


        Bukkit.getScheduler().runTaskTimer(this, naturalSeedFallTask, interval, interval);
    }

    public void startPlantingTask() {
        int interval = configService.getScanTicks();

        Bukkit.getScheduler().runTaskTimer(this, new PlantingTask(this, plantingService), interval, interval);
    }

    public void checkForUpdates() {
        this.updateChecker = new UpdateChecker(this, UpdateCheckSource.GITHUB_RELEASE_TAG, "Jochyoua/AutoRoot")
                .checkEveryXHours(24)
                .setNotifyOpsOnJoin(true)
                .setNotifyByPermissionOnJoin("autoroot.checkupdate")
                .checkNow();
    }

    public double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    private void setupConfig() {

        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);

        config.options().header(
                "AutoRoot by Jochyoua\n" +
                        "Github: https://github.com/Jochyoua/AutoRoot"
        );

        saveDefaultConfig();
    }


    public void reloadPlugin() {
        getLogger().info("Reloading AutoRoot configuration...");
        if(!enableFunctionality){
            getLogger().info("Plugin functionality is disabled, but will continue reloading anyways...");
        }

        try {
            reloadConfig();
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "reloadConfig() error: ", ex);
        }

        try {
            configService.reloadConfigValues();
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Failed to reload ConfigService values: ", ex);
        }

        Bukkit.getScheduler().cancelTasks(this);


        try {
            this.plantingService = new PlantingService(this, configService);
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Failed to recreate PlantingService: ", ex);
        }

        try {
            if (this.rootListener != null) HandlerList.unregisterAll(this.rootListener);
            if (configService.isUseEventListenersForItemDrops()) {
                this.rootListener = new RootListener(this, this.plantingService);
                getServer().getPluginManager().registerEvents(this.rootListener, this);
            } else {
                startPlantingTask();
            }
            startNaturalSeedFallTask();
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Failed to re-register RootListener or Initiate Tasks: ", ex);
        }
        if (debugMessageHandler != null) {
            debugMessageHandler = new DebugMessageHandler(this, configService.isEnableDebug());
        }

        getLogger().info("AutoRoot reload complete");
    }

    @Override
    public void onDisable() {
        getLogger().info("AutoRoot disabling");
        Bukkit.getScheduler().cancelTasks(this);

        HandlerList.unregisterAll(this);
        this.rootListener = null;
        this.plantingService = null;
        this.configService = null;
    }

}
