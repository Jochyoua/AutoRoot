package io.github.jochyoua.autoroot.debug;

import io.github.jochyoua.autoroot.AutoRoot;

import java.util.logging.Level;

public class DebugMessageHandler {

    private final AutoRoot plugin;
    private final boolean debugEnabled;


    public DebugMessageHandler(AutoRoot plugin, boolean debugEnabled) {
        this.plugin = plugin;
        this.debugEnabled = debugEnabled;
    }

    public void debugMessage(String msg) {
        if (!debugEnabled) return;

        plugin.getLogger().log(Level.INFO, "[DEBUG]: {0}", msg);
    }
}
