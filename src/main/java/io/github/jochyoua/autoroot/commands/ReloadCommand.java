package io.github.jochyoua.autoroot.commands;

import io.github.jochyoua.autoroot.AutoRoot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;


public class ReloadCommand implements SubCommand {

    private final AutoRoot plugin;

    public ReloadCommand(AutoRoot plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        try {
            plugin.reloadPlugin();

            String successMessage = SubCommand.buildHeader("AutoRoot Reload") + "\n" +
                    "&eStatus: &aSuccess\n" +
                    "&eMessage: &fPlugin configuration reloaded successfully.\n" +
                    LINE_BREAK;

            sender.sendMessage(SubCommand.colorString(successMessage));
        } catch (Exception ex) {
            String failureMessage = SubCommand.buildHeader("AutoRoot Reload") + "\n" +
                    "&eStatus: &cFailed\n" +
                    "&eMessage: &cReload failed. Check server console for details.\n" +
                    LINE_BREAK;

            sender.sendMessage(SubCommand.colorString(failureMessage));
            plugin.getLogger().warning("AutoRoot reload failed: " + ex.getMessage());
        }
        return true;
    }
    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return "Reload all AutoRoot configuration files and plantable rules.";
    }
}
