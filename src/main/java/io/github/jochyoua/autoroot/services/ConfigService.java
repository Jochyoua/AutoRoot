package io.github.jochyoua.autoroot.services;

import io.github.jochyoua.autoroot.AutoRoot;
import io.github.jochyoua.autoroot.PlantableRule;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

@Getter
public final class ConfigService {

    private final AutoRoot plugin;
    private final Set<Biome> defaultBiomeWhitelist = new HashSet<>();
    private final Map<Material, PlantableRule> plantablesByBlock = new EnumMap<>(Material.class);
    private final Map<Material, List<PlantableRule>> plantablesByItem = new EnumMap<>(Material.class);
    private final Set<Material> ignoredVegetationBlocks = new HashSet<>();
    private final Set<Tag<Material>> ignoredVegetationTags = new HashSet<>();
    private Set<String> defaultCommands = new HashSet<>();
    private final List<String> defaultValidBlocks = new ArrayList<>();
    private int delayTicks;
    private int scanTicks;
    private int fallingSeedsIntervalTicks;
    private boolean enableParticles;
    private boolean enableSound;
    private boolean enableDebug;
    private boolean ignoreVegetation;
    private boolean useEventListenersForItemDrops;
    private double defaultPlantChance;
    private boolean fallingSeedsEnabled;
    private int naturalSeedFallWeight;
    private int maxLeafScanHeight;

    private boolean leafCacheDynamicScalingEnabled;
    private long leafCacheBaseLifespanMs;
    private long leafCacheMinLifespanMs;
    private long leafCacheMaxLifespanMs;

    private double windDirectionX;
    private double windDirectionZ;
    private double windStrength;
    private boolean consumeSeedOnCreation;
    private int maxSeedsPerCycle;
    private int maxChunksPerCyclePerPlayer;
    private int minimumCanopy;
    private boolean defaultEnableFallingSeeds;
    private boolean requireNearestPlayerCanBuild;
    private int maxSeedFallWeight;
    private boolean enableCoreprotect;
    private boolean defaultDestroyItemOnFailure;
    private Set<Particle> defaultSuccessParticles;
    private Set<Particle>  defaultFailureParticles;
    private Set<Sound> defaultSuccessSounds;
    private Set<Sound>  defaultFailureSounds;
    private long chunkLeafDensityLimit;

    public ConfigService(AutoRoot plugin) {
        this.plugin = plugin;
    }


    public void reloadConfigValues() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        loadBasic(cfg);
        loadDefaultBiomes(cfg);
        loadPlantables(cfg);
        loadIgnoreVegetation(cfg);

        if (!requireNearestPlayerCanBuild) {
            plugin.getLogger().warning("Seeds will NOT respect claims, WorldGuard regions, or protection plugins when planting.");
        }
    }

    private void loadDefaultBiomes(FileConfiguration cfg) {
        defaultBiomeWhitelist.clear();

        if (!cfg.isList("defaults.biome_whitelist")) {
            return;
        }

        for (String raw : cfg.getStringList("defaults.biome_whitelist")) {
            Biome biome = parseBiome(raw);
            if (biome != null) {
                defaultBiomeWhitelist.add(biome);
            }
        }
    }


    private void loadBasic(FileConfiguration cfg) {
        delayTicks = cfg.getInt("planting.delay_ticks", 80);
        scanTicks = cfg.getInt("planting.scan_interval_ticks", 20);
        useEventListenersForItemDrops = cfg.getBoolean("planting.enable_event_listeners_for_item_drops", false);
        enableDebug = cfg.getBoolean("planting.debug", false);
        requireNearestPlayerCanBuild = cfg.getBoolean("planting.require_nearest_player_can_build", true);
        enableCoreprotect = cfg.getBoolean("planting.enable_coreprotect_logging", true);

        enableParticles = cfg.getBoolean("planting.enable_particles", true);
        enableSound = cfg.getBoolean("planting.enable_sound", true);

        fallingSeedsEnabled = cfg.getBoolean("falling_seeds.enabled", true);

        maxLeafScanHeight = cfg.getInt("falling_seeds.max_leaf_scan_height", 128);
        fallingSeedsIntervalTicks = cfg.getInt("falling_seeds.interval_ticks", 120);
        consumeSeedOnCreation = cfg.getBoolean("falling_seeds.consume_leaf_on_seed_spawn", true);
        minimumCanopy = cfg.getInt("falling_seeds.minimum_canopy", 2);

        naturalSeedFallWeight = cfg.getInt("falling_seeds.natural_seed_fall_weight", 15);
        maxSeedFallWeight = cfg.getInt("falling_seeds.max_seed_fall_weight", 1000);

        maxSeedsPerCycle = cfg.getInt("falling_seeds.max_seeds_per_cycle", 32);
        maxChunksPerCyclePerPlayer = cfg.getInt("falling_seeds.max_chunks_per_cycle_per_player", 10);
        chunkLeafDensityLimit = cfg.getLong("falling_seeds.chunk_leaf_density_limit", 100);
        leafCacheDynamicScalingEnabled = cfg.getBoolean("falling_seeds.leaf_cache.dynamic_scaling_enabled", true);
        leafCacheBaseLifespanMs = cfg.getLong("falling_seeds.leaf_cache.base_lifespan_ms", 60000L);
        leafCacheMinLifespanMs = cfg.getLong("falling_seeds.leaf_cache.min_lifespan_ms", 10000L);
        leafCacheMaxLifespanMs = cfg.getLong("falling_seeds.leaf_cache.max_lifespan_ms", 60000L);

        windDirectionX = cfg.getDouble("falling_seeds.wind.direction_x", 0.1);
        windDirectionZ = cfg.getDouble("falling_seeds.wind.direction_z", -0.05);
        windStrength = cfg.getDouble("falling_seeds.wind.strength", 1.0);

        ignoreVegetation = cfg.getBoolean("ignore_vegetation.enabled", true);

        defaultPlantChance = cfg.getDouble("defaults.plant_chance", .75);
        defaultValidBlocks.clear();
        defaultValidBlocks.addAll(cfg.getStringList("defaults.valid_blocks"));
        defaultEnableFallingSeeds = cfg.getBoolean("defaults.enable_falling_seeds", false);
        defaultDestroyItemOnFailure = cfg.getBoolean("defaults.destroy_item_on_failure", false);
        defaultSuccessParticles = parseEnum(Particle.class, cfg.getStringList("defaults.success_particles"));
        defaultFailureParticles = parseEnum(Particle.class, cfg.getStringList("defaults.failure_particles"));
        defaultSuccessSounds = parseEnum(Sound.class, cfg.getStringList("defaults.success_sounds"));
        defaultFailureSounds = parseEnum(Sound.class, cfg.getStringList("defaults.failure_sounds"));
        defaultCommands.addAll(cfg.getStringList("defaults.commands"));
    }

    private void loadIgnoreVegetation(FileConfiguration cfg) {
        ignoredVegetationBlocks.clear();
        ignoredVegetationTags.clear();

        if (!ignoreVegetation) {
            return;
        }

        ConfigurationSection section = cfg.getConfigurationSection("ignore_vegetation");

        if (section == null) {
            plugin.getLogger().log(Level.WARNING, "[AutoRoot] ignore_vegetation section missing");
            return;
        }

        if (!section.isList("list")) {
            plugin.getLogger().log(Level.WARNING, "[AutoRoot] ignore_vegetation.list missing or not a list");
            return;
        }

        for (String entry : section.getStringList("list")) {
            String raw = entry.trim().toLowerCase(Locale.ROOT);
            boolean loaded = false;

            NamespacedKey key = NamespacedKey.minecraft(raw);
            Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, key, Material.class);

            if (tag != null) {
                ignoredVegetationTags.add(tag);
                loaded = true;
            }

            if (!loaded) {
                Material mat = Material.matchMaterial(raw.toUpperCase(Locale.ROOT));
                if (mat != null) {
                    ignoredVegetationBlocks.add(mat);
                    loaded = true;
                }
            }

            if (!loaded) {
                plugin.getLogger().log(Level.WARNING, "[AutoRoot] Unknown ignore_vegetation entry: {0}", raw);
            }
        }
    }


    private Set<Biome> loadBiomes(List<String> list) {
        Set<Biome> result = new HashSet<>();
        if (list.isEmpty()) result.addAll(defaultBiomeWhitelist);
        for (String s : list) {
            Biome biome = parseBiome(s);
            if (biome != null) result.add(biome);
        }
        return result;
    }

    private Biome parseBiome(String s) {
        if (s == null || s.isEmpty()) return null;

        try {
            return Biome.valueOf(s.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "[AutoRoot] Invalid biome: {0}", s);
            return null;
        }
    }

    private void loadPlantables(FileConfiguration cfg) {
        plantablesByBlock.clear();
        plantablesByItem.clear();

        if (!cfg.isConfigurationSection("plantables")) {
            plugin.debugMessage("Plantables is not a configuration section?");
            return;
        }

        for (String key : cfg.getConfigurationSection("plantables").getKeys(false)) {
            Material block = parseMaterial(key);
            if (block == null) continue;


            String base = "plantables." + key;

            if(!block.isBlock()) {
                plugin.getLogger().log(Level.WARNING, "Found entry without a valid placeable block. This rule will be ignored for: {0}", base);
                continue;
            }

            Set<Material> validBlocks = loadMaterialList(cfg.getStringList(base + ".valid_blocks"));
            Set<Material> items = loadMaterialList(cfg.getStringList(base + ".items"));
            Set<Biome> validBiomes = loadBiomes(cfg.getStringList(base + ".biome_whitelist"));

            double chance = cfg.getDouble(base + ".plant_chance", defaultPlantChance);
            boolean enableFallingSeeds = cfg.getBoolean(base + ".enable_falling_seeds", defaultEnableFallingSeeds);
            boolean destroyItemOnFailure = cfg.getBoolean(base + ".destroy_item_on_failure", defaultDestroyItemOnFailure);

            Set<String> commandsToExecute;
            if(cfg.isSet(base + ".commands")){
                commandsToExecute = new HashSet<>(cfg.getStringList(base + ".commands"));
            }else {
                commandsToExecute = defaultCommands;
            }
            Set<Particle> successParticles;
            if (!cfg.isSet(base + ".success_particles")) {
                successParticles = defaultSuccessParticles;
            } else {
                successParticles = parseEnum(Particle.class, cfg.getStringList(base + ".success_particles"));
            }

            Set<Particle> failureParticles;
            if (!cfg.isSet(base + ".failure_particles")) {
                failureParticles = defaultFailureParticles;
            } else {
                failureParticles = parseEnum(Particle.class, cfg.getStringList(base + ".failure_particles"));
            }

            Set<Sound> successSounds;
            if (!cfg.isSet(base + ".success_sounds")) {
                successSounds = defaultSuccessSounds;
            } else {
                successSounds = parseEnum(Sound.class, cfg.getStringList(base + ".success_sounds"));
            }

            Set<Sound> failureSounds;
            if (!cfg.isSet(base + ".failure_sounds")) {
                failureSounds = defaultFailureSounds;
            } else {
                failureSounds = parseEnum(Sound.class, cfg.getStringList(base + ".failure_sounds"));
            }


            PlantableRule rule = PlantableRule.builder()
                    .plantBlock(block)
                    .triggerItems(items)
                    .validBlocksBelow(validBlocks)
                    .plantChance(chance)
                    .whitelistedBiomes(validBiomes)
                    .enableFallingSeeds(enableFallingSeeds)
                    .commandsToExecute(loadCommands(commandsToExecute))
                    .destoryItemsOnFailure(destroyItemOnFailure)
                    .successParticles(successParticles)
                    .failureParticles(failureParticles)
                    .successSounds(successSounds)
                    .failureSounds(failureSounds)
                    .build();

            plantablesByBlock.put(block, rule);

            items.forEach(i -> plantablesByItem.computeIfAbsent(i, k -> new ArrayList<>()).add(rule));

            plugin.debugMessage("Loaded plantable rule: " + rule);
        }
    }

    private <T extends Enum<T>> Set<T> parseEnum(Class<T> type, List<String> list) {
        Set<T> result = new HashSet<>();

        for (String raw : list) {
            try {
                T value = Enum.valueOf(type, raw.trim().toUpperCase());
                result.add(value);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().log(
                        Level.WARNING,
                        "Invalid {0}: {1}",
                        new Object[]{ type.getSimpleName(), raw }
                );
            }
        }

        return result;
    }

    private Set<String> loadCommands(Set<String> stringList) {
        if (stringList == null || stringList.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> commands = new LinkedHashSet<>();
        for (String cmd : stringList) {
            if (cmd != null && !cmd.trim().isEmpty()) {
                String cleanCmd = cmd.trim();
                if (cleanCmd.startsWith("/")) {
                    cleanCmd = cleanCmd.substring(1);
                }
                commands.add(cleanCmd);
            }
        }
        return commands;
    }


    public Material parseMaterial(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        try {
            return Material.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Invalid material: {0}", raw);
            return null;
        }
    }


    private Set<Material> loadMaterialList(List<String> list) {
        Set<Material> result = new HashSet<>();
        if (list.isEmpty()) list = defaultValidBlocks;
        for (String s : list) {
            Material m = parseMaterial(s);
            if (m != null) result.add(m);
        }
        return result;
    }

    public Optional<PlantableRule> getRuleForItem(Material item) {
        List<PlantableRule> list = plantablesByItem.get(item);
        if (list == null || list.isEmpty()) return Optional.empty();

        return Optional.of(list.get(ThreadLocalRandom.current().nextInt(list.size())));
    }

    public Optional<PlantableRule> getRuleForBlock(Material block) {
        return Optional.ofNullable(plantablesByBlock.get(block));
    }

    public Vector getWindVector() {
        return new Vector(windDirectionX, 0, windDirectionZ);
    }

}
