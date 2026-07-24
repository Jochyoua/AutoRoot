package io.github.jochyoua.autoroot.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;


public interface SubCommand {
    String LINE_BREAK = "&6&l&m----------------------------------------";

    static String getPrefix() {
        return ChatColor.YELLOW + "[AutoRoot] " + ChatColor.GRAY;
    }

    boolean execute(CommandSender sender, String[] args);

    List<String> tab(CommandSender sender, String[] args);

    default String getPermission() {
        return "AutoRoot.admin";
    }

    String getDescription();

    static String colorString(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    static String buildHeader(String title) {
        return "&r\n" + "&6&l| &b" + title + " &6&l|\n" + LINE_BREAK + "&r";
    }
}
