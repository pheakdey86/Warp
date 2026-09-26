package com.pheakdey86.ansamwarp;

import org.bukkit.plugin.java.JavaPlugin;

public final class AnsamWarp extends JavaPlugin {
    private WarpManager warpManager;
    private MessageManager messages;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages = new MessageManager(this);
        warpManager = new WarpManager(this);
        teleportManager = new TeleportManager(this, warpManager, messages);
        WarpCommand command = new WarpCommand(this, warpManager, teleportManager, messages);
        getCommand("warp").setExecutor(command);
        getCommand("warp").setTabCompleter(command);
        getCommand("setwarp").setExecutor(command);
        getCommand("setwarp").setTabCompleter(command);
        getCommand("delwarp").setExecutor(command);
        getCommand("delwarp").setTabCompleter(command);
        getCommand("ansamwarp").setExecutor(command);
        getCommand("ansamwarp").setTabCompleter(command);
        getServer().getPluginManager().registerEvents(teleportManager, this);
        getLogger().info("AnsamWarp enabled.");
    }

    @Override
    public void onDisable() {
        if (teleportManager != null) teleportManager.cancelAll();
        if (warpManager != null) warpManager.save();
    }

    public void reloadPlugin() {
        reloadConfig();
        messages.reload();
        warpManager.reload();
    }

    public WarpManager getWarpManager() { return warpManager; }
}
