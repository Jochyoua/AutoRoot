package io.github.jochyoua.autoroot.commands;

import io.github.jochyoua.autoroot.AutoRoot;
import io.github.jochyoua.autoroot.PlantableRule;
import io.github.jochyoua.autoroot.services.ConfigService;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class InfoCommand implements SubCommand {

    private final AutoRoot plugin;
    private final ConfigService config;

    public InfoCommand(AutoRoot plugin, ConfigService config){
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(args.length == 0) {
            sender.sendMessage(SubCommand.colorString(getOverallMetrics()));
            return true;
        }
        PlantableRule rule = resolveRule(sender, args[0]);
        if(rule == null) {
            return true;
        }
        if(args.length == 2 && args[1].equalsIgnoreCase("simulate")){
            sender.sendMessage(simulatePlanting(sender, rule));
        } else {
            sender.sendMessage(getPlantInfo(rule));
        }
        return true;
    }

    private String getOverallMetrics() {
        boolean enabled = plugin.isEnableFunctionality();
        int ruleCount = config.getPlantablesByBlock().size();
        int activeLeafCache = plugin.getNaturalSeedFallTask().getLeafCache().size();
        int staleChunks = plugin.getChunkScanningListener().getStaleChunkCache().size();
        int queueSize = plugin.getPlantingService().getQueue().size();
        int fallingSeedsSize = plugin.getNaturalSeedFallTask().getNaturalSeeds().size();

        return SubCommand.buildHeader("AutoRoot Info") + "\n" +
                "&6Status:\n" +
                " &7• &eFunctionality: " + (enabled ? "&aEnabled" : "&cDisabled") + "\n" +
                " &7• &eFalling Seeds: " + (config.isFallingSeedsEnabled() ? "&aEnabled" : "&cDisabled") + "\n" +
                "&6Metrics:\n" +
                " &7• &eActive Rules: &b" + ruleCount + "\n" +
                " &7• &ePlanting Queue: &b" + queueSize + "\n" +
                " &7• &eLeaf Chunks: &b" + activeLeafCache + "\n" +
                " &7• &eFalling Seeds: &b" + fallingSeedsSize + "\n" +
                " &7• &eStale Chunks: &b" + staleChunks + "\n\n" +
                "&7&oWant plantable rule details? Try &f/autoroot info <material>&7&o!\n" +
                LINE_BREAK;
    }

    private String simulatePlanting(CommandSender sender, PlantableRule rule) {
        if (!(sender instanceof Player)) {
            return SubCommand.colorString("&cSimulation requires a player.");
        }
        Player player = (Player)sender;

        Block blockBelow = player.getLocation().getBlock().getRelative(0, -1, 0);
        Block blockAtFeet = player.getLocation().getBlock();
        Biome biome = blockBelow.getBiome();

        boolean biomeAllowed = rule.getWhitelistedBiomes().isEmpty()
                || rule.getWhitelistedBiomes().contains(biome);

        boolean validBelow = rule.getValidBlocksBelow().isEmpty()
                || rule.getValidBlocksBelow().contains(blockBelow.getType());

        boolean airOrVegetation = rule.isAirOrVegetation(blockAtFeet, config);

        boolean finalResult = biomeAllowed && validBelow && airOrVegetation;

        String biomeStatus = biomeAllowed ? "&a(OK)" : "&c(Not Allowed)";
        String blockBelowStatus = validBelow ? "&a(OK)" : "&c(Invalid)";
        String feetStatus = airOrVegetation ? "&a(OK)" : "&c(Blocked)";
        String resultStatus = finalResult
                ? "&aThis location is valid for planting!"
                : "&cThis location is NOT valid for planting.";

        String message = SubCommand.buildHeader("AutoRoot Planting Simulation") + "\n" +
                "&eBiome: &f" + biome.name() + " " + biomeStatus + "\n" +
                "&eBlock Below: &f" + blockBelow.getType().name() + " " + blockBelowStatus + "\n" +
                "&eFeet Block: &f" + blockAtFeet.getType().name() + " " + feetStatus + "\n" +
                "&eChance: &f" + Math.round(rule.getPlantChance() * 100) + "%\n" +
                "&eResult: " + resultStatus + "\n" +
                LINE_BREAK;

        return SubCommand.colorString(message);
    }

    private String getPlantInfo(PlantableRule rule) {
        String triggerItems = rule.getTriggerItems().isEmpty()
                ? "None"
                : "" + rule.getTriggerItems().stream().map(Enum::name).collect(Collectors.joining(","));

        String validBlocksBelow = rule.getValidBlocksBelow().isEmpty()
                ? "Any"
                : "" + rule.getValidBlocksBelow().stream().map(Enum::name).collect(Collectors.joining(", "));

        String commandList = rule.getValidBlocksBelow().isEmpty()
                ? "None"
                :  String.join(", ", rule.getCommandsToExecute());

        String whitelistedBiomes;
        if (rule.getWhitelistedBiomes().isEmpty()) {
            whitelistedBiomes = "All";
        } else {
            StringBuilder biomeList = new StringBuilder();

            for (Biome biome : rule.getWhitelistedBiomes()) {
                if (biomeList.length() > 0) {
                    biomeList.append(", ");
                }
                biomeList.append(biome.name());
            }

            whitelistedBiomes = biomeList.toString();
        }

        String message = SubCommand.buildHeader("AutoRoot Plantable Rule") + "\n" +
                "&ePlant Block: &f" + rule.getPlantBlock().name() + "\n" +
                "&eTrigger Items: &f" + triggerItems + "\n" +
                "&eValid Blocks Below: &f" + validBlocksBelow + "\n" +
                "&eWhitelisted Biomes: &f" + whitelistedBiomes + "\n" +
                "&eCommand List: &f" + commandList + "\n" +
                "&ePlant Chance: &f" + Math.round(rule.getPlantChance() * 100) + "%\n" +
                "&eFalling Seeds: &f" + (rule.isEnableFallingSeeds() ? "Enabled" : "Disabled") + "\n" +
                LINE_BREAK;

        return SubCommand.colorString(message);
    }

    private PlantableRule resolveRule(CommandSender sender, String arg) {

        Material mat;
        try {
            mat = Material.valueOf(arg.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(SubCommand.getPrefix()
                    + ChatColor.RED + arg + " is not a valid material!");
            return null;
        }

        return config.getRuleForBlock(mat).orElseGet(() -> {
            sender.sendMessage(SubCommand.getPrefix()
                    + ChatColor.RED + mat.name() + " has no plantable rule.");
            return null;
        });
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        if (subArgs.length == 1) {
            String partial = subArgs[0].toUpperCase(Locale.ROOT);
            return config.getPlantablesByBlock().keySet().stream()
                    .map(Enum::name)
                    .filter(name -> name.startsWith(partial))
                    .collect(Collectors.toList());
        }

        if (subArgs.length == 2) {
            String partial = subArgs[1].toLowerCase(Locale.ROOT);
            return List.of("simulate").stream()
                    .filter(s -> s.startsWith(partial))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Display plantable rules or simulate planting at your current location.";
    }


}
