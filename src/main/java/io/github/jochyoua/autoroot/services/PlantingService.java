package io.github.jochyoua.autoroot.services;

import io.github.jochyoua.autoroot.AutoRoot;
import io.github.jochyoua.autoroot.PlantQueue;
import io.github.jochyoua.autoroot.PlantableRule;
import io.github.jochyoua.autoroot.commands.SubCommand;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class PlantingService {

    private final AutoRoot plugin;
    private final ConfigService config;
    @Getter
    private final ArrayDeque<PlantQueue> queue = new ArrayDeque<>();


    public PlantingService(AutoRoot plugin, ConfigService config) {
        this.plugin = plugin;
        this.config = config;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEnableFunctionality()) return;
                processQueue();
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void processQueue() {
        if (queue.isEmpty()) return;

        for (PlantQueue item : queue) {
            item.decrementTicks();
        }

        while (!queue.isEmpty() && queue.peek().isReady()) {
            PlantQueue attempt = queue.poll();
            if (attempt != null) {
                performPlantingNow(attempt.item, attempt.getRule());
            }
        }
    }


    public void queuePlantingAttempt(Item droppedItem) {
        if (droppedItem == null || droppedItem.isDead()) return;

        Material mat = droppedItem.getItemStack().getType();

        config.getRuleForItem(mat).ifPresent(rule -> queue.add(new PlantQueue(droppedItem, rule, config.getDelayTicks())));
    }

    public void performPlantingNow(Item item, PlantableRule rule) {
        if (item == null || !item.isValid() || item.isDead()) return;

        Location loc = item.getLocation();
        if (loc.getWorld() == null) return;

        Block target = getTargetBlock(loc, rule);
        Block soil = target.getRelative(0, -1, 0);


        if (!isValidSoil(rule, soil)) {
            return;
        }

        if (!rule.isBiomeAllowed(soil)) {
            return;
        }

        if (!rule.passesChance(plugin.randomDouble(), config.getDefaultPlantChance())) {
            playFailureEffects(target);
            if (rule.isDestoryItemsOnFailure()) {
                removeOrDecreaseItem(item);
            }
            return;
        }

        if (!rule.isAirOrVegetation(target, config)) {
            return;
        }

        if (!tryPlant(item, rule, target)) {
            playFailureEffects(target);
        }
    }

    private Block getTargetBlock(Location loc, PlantableRule rule) {
        Block block = loc.getBlock();
        Material type = block.getType();

        if (type.isAir() || rule.isVegetation(type, config)) {
            return block;
        }

        return block.getRelative(BlockFace.UP);
    }

    public boolean isValidSoil(PlantableRule rule, Block soil) {

        Block current = soil;

        if (config.isIgnoreVegetation()) {
            for (int i = 0; i < 2; i++) {
                Material type = current.getType();

                if (config.getIgnoredVegetationBlocks().contains(type)) {
                    current = current.getRelative(BlockFace.DOWN);
                    continue;
                }

                boolean matchedTag = false;
                for (Tag<Material> tag : config.getIgnoredVegetationTags()) {
                    if (tag.isTagged(type)) {
                        current = current.getRelative(BlockFace.DOWN);
                        matchedTag = true;
                        break;
                    }
                }

                if (!matchedTag) break;
            }
        }

        return rule.getValidBlocksBelow().isEmpty() || rule.getValidBlocksBelow().contains(current.getType());
    }

    public Player getNearestPlayer(Location loc) {
        Player nearest = null;
        double bestDistance = 4096;
        World world = loc.getWorld();

        if (world == null) {
            plugin.debugMessage("World for location " + loc + " is null.");
            return null;
        }

        for (Player player : world.getPlayers()) {
            double distance = player.getLocation().distanceSquared(loc);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = player;
            }
        }

        return nearest;
    }


    private boolean placeItemInWorld(Block target, PlantableRule rule) {
        Material plantMaterial = rule.getPlantBlock();

        Player player = getNearestPlayer(target.getLocation());
        if (config.isRequireNearestPlayerCanBuild()) {
            if (player == null) {
                return false;
            }

            if (!rule.getPlantBlock().isBlock()) {
                plugin.debugMessage(rule.getPlantBlock() + " is not a placeable block, using AIR as placeholder for event.");
            }

            BlockState replacedState = target.getState();

            BlockPlaceEvent event = new BlockPlaceEvent(target, replacedState, target.getRelative(BlockFace.DOWN), new ItemStack(rule.getPlantBlock().isBlock() ? plantMaterial : Material.AIR), player, true, EquipmentSlot.HAND);

            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                plugin.debugMessage("Failed to invoke event, was cancelled. For player " + player.getName() + "\n" + rule);
                return false;
            }
        }
        if (config.isEnableCoreprotect()) {
            CoreProtectAPI api = plugin.getCoreProtect();
            if (api != null) {
                api.logPlacement("AutoRoot", target.getLocation(), plantMaterial, rule.getPlantBlock().createBlockData());
            }
        }


        target.setType(plantMaterial);
        executeRuleCommands(rule, player, target);
        return true;
    }

    private void executeRuleCommands(PlantableRule rule, Player player, Block targetBlock) {
        if (rule.getCommandsToExecute() == null || rule.getCommandsToExecute().isEmpty()) return;
        for (String command : rule.getCommandsToExecute()) {
            boolean asPlayer = command.contains("[asPlayer]");
            command = command.replace("[asPlayer]", "");

            if (command.startsWith("[randomEntry]")) {
                String content = command.replace("[randomEntry]", "");

                String[] choices = content.split("\\|");
                int randomIndex = ThreadLocalRandom.current().nextInt(choices.length);
                command = choices[randomIndex].trim();
            }

            if (player == null && asPlayer) {
                plugin.debugMessage("Failed to execute command " + rule + " - No nearby players.");
                return;
            }
            if (player != null) {
                command = command.replace("%nearest_player_name%", player.getName());
                command = command.replace("%nearest_player_uuid%", player.getUniqueId().toString());
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    command = PlaceholderAPI.setPlaceholders(player, command);
                } else {
                    plugin.debugMessage("Player was detected BUT PlaceholderAPI is not installed.");
                }
            }
            command = SubCommand.colorString(command.replace("%x%", String.valueOf(targetBlock.getX())).replace("%y%", String.valueOf(targetBlock.getY())).replace("%z%", String.valueOf(targetBlock.getZ())).replace("%world%", targetBlock.getWorld().getName()));

            if (asPlayer) {
                player.performCommand(command);
            } else {
                if (command.contains("%nearest_player_name%") || command.contains("%nearest_player_uuid%")) {
                    plugin.debugMessage("Command " + command + " requires a player but no one was nearby! :(");
                    return;
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }

        }
    }

    private boolean tryPlant(Item item, PlantableRule rule, Block target) {
        try {
            if (!placeItemInWorld(target, rule)) return false;

            removeOrDecreaseItem(item);

            playSuccessEffects(target);
            return true;
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, String.format("Failed setting block for planting: %s", rule.getPlantBlock()), ex);
            return false;
        }
    }

    private void removeOrDecreaseItem(Item item) {
        ItemStack stack = item.getItemStack();
        if (stack.getAmount() > 1) {
            stack.setAmount(stack.getAmount() - 1);
            item.setItemStack(stack);
        } else {
            item.remove();
        }
    }

    private void playFailureEffects(Block target) {
        World world = target.getWorld();
        Location center = target.getLocation().add(0.5, 0.5, 0.5);

        if (config.isEnableParticles()) {
            world.spawnParticle(Particle.ASH, center, 12, 0.25, 0.25, 0.25, 0.01);
            world.spawnParticle(Particle.SMOKE_NORMAL, center, 8, 0.2, 0.2, 0.2, 0.03);

            Material dirtType = target.getType().isAir() ? Material.DIRT : target.getType();
            world.spawnParticle(Particle.BLOCK_CRACK, center, 10, 0.2, 0.1, 0.2, 0.05, dirtType.createBlockData());
        }

        if (config.isEnableSound()) {
            world.playSound(center, Sound.BLOCK_WOOD_BREAK, 0.5f, 0.6f);
            world.playSound(center, Sound.BLOCK_ROOTED_DIRT_BREAK, 0.7f, 0.8f);
        }
    }

    private void playSuccessEffects(Block target) {
        World world = target.getWorld();
        Location center = target.getLocation().add(0.5, 0.5, 0.5);

        if (config.isEnableParticles()) {
            world.spawnParticle(Particle.VILLAGER_HAPPY, center, 8, 0.2, 0.2, 0.2, 0.02);

            world.spawnParticle(Particle.COMPOSTER, center, 6, 0.15, 0.2, 0.15, 0.05);

            world.spawnParticle(Particle.BLOCK_CRACK, center.clone().add(0, 0.2, 0), 6, 0.1, 0.1, 0.1, 0.02, Material.OAK_LEAVES.createBlockData());
        }

        if (config.isEnableSound()) {
            world.playSound(center, Sound.ITEM_BONE_MEAL_USE, 0.8f, 1.1f);
            world.playSound(center, Sound.BLOCK_AZALEA_LEAVES_PLACE, 0.7f, 1.3f);
        }
    }
}
