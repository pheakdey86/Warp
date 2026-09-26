package com.pheakdey86.ansamwarp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class WarpManager {
    private final AnsamWarp plugin;
    private final File file;
    private YamlConfiguration data;

    public WarpManager(AnsamWarp plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "warps.yml");
        reload();
    }

    public void reload() {
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { plugin.getLogger().warning("Could not create warps.yml: " + e.getMessage()); }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try { data.save(file); } catch (IOException e) { plugin.getLogger().warning("Could not save warps.yml: " + e.getMessage()); }
    }

    public boolean exists(String name) { return data.isConfigurationSection("warps." + name.toLowerCase(Locale.ROOT)); }

    public Location get(String name) {
        ConfigurationSection s = data.getConfigurationSection("warps." + name.toLowerCase(Locale.ROOT));
        if (s == null) return null;
        World world = Bukkit.getWorld(s.getString("world", ""));
        if (world == null) return null;
        return new Location(world, s.getDouble("x"), s.getDouble("y"), s.getDouble("z"), (float)s.getDouble("yaw"), (float)s.getDouble("pitch"));
    }

    public void set(String name, Location loc) {
        String path = "warps." + name.toLowerCase(Locale.ROOT);
        data.set(path + ".world", Objects.requireNonNull(loc.getWorld()).getName());
        data.set(path + ".x", loc.getX());
        data.set(path + ".y", loc.getY());
        data.set(path + ".z", loc.getZ());
        data.set(path + ".yaw", loc.getYaw());
        data.set(path + ".pitch", loc.getPitch());
        save();
    }

    public boolean remove(String name) {
        String path = "warps." + name.toLowerCase(Locale.ROOT);
        if (!data.contains(path)) return false;
        data.set(path, null);
        save();
        return true;
    }

    public List<String> names() {
        ConfigurationSection s = data.getConfigurationSection("warps");
        if (s == null) return new ArrayList<>();
        List<String> list = new ArrayList<>(s.getKeys(false));
        list.sort(String.CASE_INSENSITIVE_ORDER);
        return list;
    }
}
