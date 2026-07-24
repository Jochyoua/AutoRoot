package io.github.jochyoua.autoroot.commands;

import io.github.jochyoua.autoroot.AutoRoot;
import io.github.jochyoua.autoroot.services.ConfigService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.stream.Collectors;

public final class AutoRootCommand implements CommandExecutor, TabCompleter {


    private final Map<String, SubCommand> subcommands = new HashMap<>();

    public AutoRootCommand(AutoRoot plugin, ConfigService configService) {
        subcommands.put("reload", new ReloadCommand(plugin));
        subcommands.put("info", new InfoCommand(plugin, configService));
        subcommands.put("list", new ListCommand(configService));
        subcommands.put("version", new VersionCommand(plugin, configService));
        subcommands.put("toggle", new ToggleCommand(plugin));
    }

    private String getCommandUsage(CommandSender sender) {
        String commandsList = subcommands.entrySet().stream()
                .filter(e -> sender.hasPermission(e.getValue().getPermission()))
                .map(e -> "&e/autoroot " + e.getKey() + " &7- &f" + e.getValue().getDescription())
                .collect(Collectors.joining("\n"));

        if (commandsList.isEmpty()) {
            commandsList = "&cYou do not have permission to execute any AutoRoot commands.";
        }

        return SubCommand.colorString(SubCommand.buildHeader("AutoRoot Commands") + "\n"
                + commandsList + "\n"
                + SubCommand.LINE_BREAK);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(getCommandUsage(sender));
            return true;
        }

        SubCommand sub = subcommands.get(args[0].toLowerCase());
        if (sub == null) {
            sender.sendMessage(SubCommand.getPrefix() + ChatColor.RED + "Unknown subcommand.");
            sender.sendMessage(getCommandUsage(sender));
            return true;
        }
        if (!sender.hasPermission(sub.getPermission())) {
            sender.sendMessage(SubCommand.getPrefix() + ChatColor.RED + "Permission denied " + ChatColor.GRAY + "(" + ChatColor.YELLOW + sub.getPermission() + ChatColor.GRAY + ")");
            return true;
        }
        return sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if (args.length == 1) {
            String partial = args[0].toLowerCase();

            return subcommands.entrySet().stream()
                    .filter(e -> sender.hasPermission(e.getValue().getPermission()))
                    .map(Map.Entry::getKey)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        SubCommand sub = subcommands.get(args[0].toLowerCase());
        if (!sender.hasPermission(sub.getPermission())) return Collections.emptyList();
        return sub.tab(sender, args);
    }
}

