package io.github.jochyoua.autoroot.commands;

import io.github.jochyoua.autoroot.data.PlantableRule;
import io.github.jochyoua.autoroot.services.ConfigService;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class ListCommand implements SubCommand{
    private final ConfigService config;

    public ListCommand(ConfigService config) {
        this.config = config;
    }
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (config.getPlantablesByBlock().isEmpty()) {
            String emptyMessage = SubCommand.buildHeader("AutoRoot Plantables") + "\n" +
                    "&cNo plantable rules configured.\n" +
                    LINE_BREAK;
            sender.sendMessage(SubCommand.colorString(emptyMessage));
            return true;
        }
        Map<Material, PlantableRule> plantableRules = config.getPlantablesByBlock();
        StringBuilder listBuilder = new StringBuilder();
        listBuilder.append(SubCommand.buildHeader("AutoRoot Plantables ("+plantableRules.size()+")")).append("\n");

        plantableRules.forEach((material, rule) -> listBuilder.append("&e- &f").append(material.name())
                .append(" &7(").append(Math.round(rule.getPlantChance() * 100)).append("% chance)\n"));

        listBuilder.append(LINE_BREAK);

        sender.sendMessage(SubCommand.colorString(listBuilder.toString()));
        return true;
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "List all materials currently configured with plantable rules.";
    }
}
