package io.github.jochyoua.autoroot.tasks;

import io.github.jochyoua.autoroot.AutoRoot;
import io.github.jochyoua.autoroot.data.ChunkInfo;
import io.github.jochyoua.autoroot.data.LeafInfo;
import io.github.jochyoua.autoroot.data.PlantableRule;
import io.github.jochyoua.autoroot.enums.LeafFailReasonEnum;
import io.github.jochyoua.autoroot.services.ConfigService;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;



public class NaturalSeedFallTask implements Runnable {

    private final AutoRoot plugin;
    private final ConfigService config;
    @Getter
    private final Map<Long, ChunkInfo> chunkCache = new ConcurrentHashMap<>();
    @Getter
    private final Set<Item> naturalSeeds = new HashSet<>();
    private long lastCycle = System.currentTimeMillis();
    private long lastLeafCacheReset = System.currentTimeMillis();
    private long cacheLifespan;
    private int seedsThisCycle = 0;


    public NaturalSeedFallTask(AutoRoot plugin, ConfigService config) {
        this.plugin = plugin;
        this.config = config;

        this.cacheLifespan = config.getLeafCacheBaseLifespanMs();
    }

    public void resetCaches() {
        chunkCache.clear();
        naturalSeeds.clear();
    }

    @Override
    public void run() {
        if(!plugin.isEnableFunctionality()) return;
        if (!config.isFallingSeedsEnabled()) return;
        long now = System.currentTimeMillis();

        if (now - lastCycle >= config.getFallingSeedsIntervalTicks() * 50L) {
            seedsThisCycle = 0;
            lastCycle = now;
        }

        int playerSize = Bukkit.getOnlinePlayers().size();

        if (config.isLeafCacheDynamicScalingEnabled()) {
            long max = config.getLeafCacheMaxLifespanMs();
            long min = config.getLeafCacheMinLifespanMs();
            long base = config.getLeafCacheBaseLifespanMs();

            long dynamic = base / Math.max(1, playerSize);
            cacheLifespan = Math.min(max, Math.max(min, dynamic));
        }

        if (now - lastLeafCacheReset >= cacheLifespan) {
            if(playerSize != 0) {
                plugin.debugMessage("Leaf Cache reset after " + cacheLifespan + "ms (" + playerSize + " players)");
            }
            chunkCache.clear();
            lastLeafCacheReset = now;
        }

        naturalSeeds.removeIf(item -> {
            if (item.getTicksLived() > 120) {
                item.remove();
                return true;
            }
            return false;
        });

        processChunks(getChunksAroundPlayers());
    }

    private Set<Chunk> getChunksAroundPlayers() {
        Set<Chunk> listOfValidChunks = new HashSet<>();

        for (World world : Bukkit.getWorlds()) {
            for (Player p : world.getPlayers()) {
                Chunk center = p.getLocation().getChunk();
                int cx = center.getX();
                int cz = center.getZ();

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        Chunk chunk = world.getChunkAt(cx + dx, cz + dz);
                        if (chunk.isLoaded()) {
                            listOfValidChunks.add(chunk);
                        }
                    }
                }
            }
        }
        return listOfValidChunks;
    }


    private void processChunks(Set<Chunk> chunksToProcess) {
        int processed = 0;
        int maxChunksThisCycle = Bukkit.getOnlinePlayers().size() * config.getMaxChunksPerCyclePerPlayer();
        for (Chunk chunk : chunksToProcess) {
            if (processed >= maxChunksThisCycle) {
                plugin.debugMessage("Too many chunks this cycle: " + processed + "/" + maxChunksThisCycle);
                break;
            }

            if (seedsThisCycle >= config.getMaxSeedsPerCycle()) {
                plugin.debugMessage("Too many seeds this cycle: " + seedsThisCycle + "/" + config.getMaxSeedsPerCycle());
                break;
            }

            long key = ChunkInfo.convertKey(chunk);

            if (!chunkCache.containsKey(key)) {
                chunkCache.put(key, scanChunk(chunk));
                continue;
            }

            if (chunkCache.get(key).getLeavesInChunk().isEmpty()) continue;

            if (chunkCache.get(key).getLeavesInChunk().size() > config.getChunkLeafDensityLimit()) {
                plugin.debugMessage("Too many leaves in chunk: "
                        + chunkCache.get(key).getLeavesInChunk().size() + "/" + config.getChunkLeafDensityLimit());
                chunkCache.get(key).getLeavesInChunk().clear();
                continue;
            }

            processed++;
            startAsyncChunkProcessing(chunk);
        }
    }

    private void applyWind(Block leaf, Item item) {
        Vector wind = config.getWindVector().multiply(config.getWindStrength() * 1.5);

        double heightFactor = Math.min(1.5, leaf.getY() / 100.0);

        double angle = plugin.randomDouble() * Math.PI * 2;
        double strength = (0.3 + plugin.randomDouble() * 0.4) * heightFactor;
        double yVel = -0.08 - plugin.randomDouble() * 0.04;

        Vector drift = new Vector(Math.cos(angle) * strength, yVel, Math.sin(angle) * strength);

        item.setVelocity(wind.add(drift));

    }

    private void applyDecisionSync(Map<String, Object> decision) {
        if (seedsThisCycle >= config.getMaxSeedsPerCycle()) {
            return;
        }

        LeafFailReasonEnum reason = (LeafFailReasonEnum) decision.get("reason");
        if (reason == LeafFailReasonEnum.CHANCE_FAIL) return;

        LeafInfo leaf = (LeafInfo) decision.get("leaf");


        PlantableRule rule = (PlantableRule) decision.get("rule");

        if (rule == null || reason != LeafFailReasonEnum.OK) {
            removeLeafFromCache((Chunk) decision.get("chunk"), leaf);
            return;
        }


        Block leafBlock = leaf.getBlock();

        if (!(leafBlock.getBlockData() instanceof Leaves)) {
            plugin.debugMessage("Leaf vanished before sync spawn: " + leaf);
            removeLeafFromCache((Chunk) decision.get("chunk"), leaf);
            return;
        }

        World world = leafBlock.getWorld();

        if (config.isConsumeSeedOnCreation()) {
            leafBlock.setType(Material.AIR);
            removeLeafFromCache((Chunk) decision.get("chunk"), leaf);
        }

        ItemStack sapling = new ItemStack(rule.getPlantBlock());
        Item item = world.dropItem(leafBlock.getLocation().add(0.5, -0.3, 0.5), sapling);
        item.setPickupDelay(200);

        applyWind(leafBlock, item);

        naturalSeeds.add(item);
        seedsThisCycle++;
    }


    private void startAsyncChunkProcessing(final Chunk chunk) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<String, Object> decision = evaluateChunkAsync(chunk);

            if (!decision.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> applyDecisionSync(decision));
            }
        });
    }


    private Map<String, Object> evaluateChunkAsync(Chunk chunk) {

        long key = ChunkInfo.convertKey(chunk);
        List<LeafInfo> leaves = chunkCache.get(key).getLeavesInChunk();
        if (leaves == null || leaves.isEmpty()) return Collections.emptyMap();

        LeafInfo leaf = pickRandomValidLeaf(leaves);
        if (leaf == null) return Collections.emptyMap();

        Material leafType = leaf.getMaterial();
        Material saplingMat = Material.matchMaterial(leafType.name().replace("_LEAVES", "_SAPLING"));
        PlantableRule rule = saplingMat != null ? config.getRuleForItem(saplingMat).orElse(null) : null;

        LeafFailReasonEnum reason = evaluateLeafAsync(leaf, rule);

        Map<String, Object> decision = new HashMap<>();
        decision.put("chunk", chunk);
        decision.put("leaf", leaf);
        decision.put("reason", reason);
        decision.put("rule", rule);

        return decision;
    }

    private LeafFailReasonEnum evaluateLeafAsync(LeafInfo leaf, PlantableRule rule) {
        if (leaf == null || rule == null) return LeafFailReasonEnum.NULL;

        if (leaf.isPersistent() || leaf.getDistance() >= 7) return LeafFailReasonEnum.DEAD_TREE;

        Biome biome = leaf.getBiome();

        if (!rule.getWhitelistedBiomes().isEmpty() && !rule.getWhitelistedBiomes().contains(biome))
            return LeafFailReasonEnum.BAD_BIOME;

        if (!rule.isEnableFallingSeeds()) return LeafFailReasonEnum.NOT_ENABLED;

        if (leaf.getDistance() <= config.getMinimumCanopy()) return LeafFailReasonEnum.BARE_CANOPY;

        int weight = config.getNaturalSeedFallWeight();
        int max = config.getMaxSeedFallWeight();

        if (max <= 0 || ThreadLocalRandom.current().nextInt(max) >= weight) {
            return LeafFailReasonEnum.CHANCE_FAIL;
        }

        return LeafFailReasonEnum.OK;
    }


    public ChunkInfo scanChunk(Chunk chunk) {
        long chunkKey = ChunkInfo.convertKey(chunk);
        if(chunkCache.containsKey(chunkKey))
            return chunkCache.get(ChunkInfo.convertKey(chunk));

        List<LeafInfo> leaves = new CopyOnWriteArrayList<>();
        World world = chunk.getWorld();

        int worldMin = world.getMinHeight();
        int worldMax = world.getMaxHeight();
        int maxLeafScanHeight = Math.min(worldMax, config.getMaxLeafScanHeight());

        int saplingCount = 0;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int highest = world.getHighestBlockYAt((chunk.getX() << 4) + x, (chunk.getZ() << 4) + z);

                int minLeafY = Math.max(highest - 20, worldMin);
                int maxLeafY = Math.min(highest + 8, maxLeafScanHeight);

                Block previousBlock = null;

                for (int y = worldMin; y < worldMax; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    Material type = block.getType();

                    if (Tag.SAPLINGS.isTagged(type)) {
                        saplingCount++;
                    }

                    if (y >= minLeafY && y <= maxLeafY) {
                        if (block.getBlockData() instanceof Leaves) {
                            Leaves data = (Leaves) block.getBlockData();
                            boolean isExposedUnderneath = (y == worldMin) ||
                                    (previousBlock != null && previousBlock.getType() == Material.AIR);

                            if (isExposedUnderneath) {
                                leaves.add(new LeafInfo(
                                        block,
                                        type,
                                        data.isPersistent(),
                                        data.getDistance(),
                                        block.getBiome()
                                ));
                            }
                        }
                    }

                    previousBlock = block;
                }
            }
        }

        return ChunkInfo.builder()
                .chunkKey(chunkKey)
                .leavesInChunk(leaves)
                .plantedSaplings(saplingCount)
                .build();
    }


    private LeafInfo pickRandomValidLeaf(List<LeafInfo> leaves) {
        if (leaves.isEmpty()) return null;

        return leaves.get(ThreadLocalRandom.current().nextInt(leaves.size()));
    }

    private void removeLeafFromCache(Chunk chunk, LeafInfo leaf) {
        long key = ChunkInfo.convertKey(chunk);
        List<LeafInfo> list = chunkCache.get(key).getLeavesInChunk();
        if (list != null) list.remove(leaf);
    }

}
