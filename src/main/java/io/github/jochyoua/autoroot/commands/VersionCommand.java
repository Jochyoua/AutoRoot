package io.github.jochyoua.autoroot.commands;

import io.github.jochyoua.autoroot.AutoRoot;
import io.github.jochyoua.autoroot.services.ConfigService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;

import java.util.Collections;
import java.util.List;

public class VersionCommand implements SubCommand {

    private final AutoRoot plugin;
    private final ConfigService config;

    public VersionCommand(AutoRoot plugin, ConfigService config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        PluginDescriptionFile desc = plugin.getDescription();

        String message = SubCommand.buildHeader("AutoRoot Version Info") + "\n" +
                String.format("&eAutoRoot v%s by %s", desc.getVersion(), String.join(", ", desc.getAuthors())) + "\n" +
                (!plugin.getUpdateChecker().isUsingLatestVersion() ? String.format("&cYour version is outdated! Latest: %s", plugin.getUpdateChecker().getLatestVersion()) + "\n" : "") +
                String.format("&eRunning on %s %s", System.getProperty("os.name"), System.getProperty("os.version")) + "\n" +
                String.format("&eJava %s by %s", System.getProperty("java.version"), System.getProperty("java.vendor")) + "\n" +
                String.format("&eServer version %s (MC: %s)", Bukkit.getVersion(), Bukkit.getBukkitVersion()) + "\n" +
                String.format("&eLoaded %d plantable rules", config.getPlantablesByBlock().size()) + "\n" +
                LINE_BREAK;


        sender.sendMessage(SubCommand.colorString(message));
        return true;
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Display plugin version, and other information.";
    }

    @Override
    public String getPermission() {
        return "autoroot.version";
    }
}