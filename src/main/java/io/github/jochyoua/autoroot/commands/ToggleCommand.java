package io.github.jochyoua.autoroot.commands;

import io.github.jochyoua.autoroot.AutoRoot;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ToggleCommand implements SubCommand{
    private final AutoRoot plugin;

    public ToggleCommand(AutoRoot plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        boolean newState = !plugin.isEnableFunctionality();
        plugin.setEnableFunctionality(newState);

        String message = SubCommand.buildHeader("AutoRoot Toggle") + "\n" +
                "&ePlugin functionality is now " + (newState ? "&aenabled&e." : "&cdisabled&e.") + "\n" +
                LINE_BREAK;

        sender.sendMessage(SubCommand.colorString(message));
        plugin.debugMessage("Plugin functionality is now " + newState +"!");

        return true;
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public String getDescription() {
        return "Toggles the AutoRoot plugin functionality on or off.";
    }

    @Override
    public String getPermission(){
        return "AutoRoot.admin.toggle";
    }
}
