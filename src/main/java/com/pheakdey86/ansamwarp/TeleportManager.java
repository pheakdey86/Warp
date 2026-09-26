package com.pheakdey86.ansamwarp;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class TeleportManager implements Listener {
    private final AnsamWarp plugin;
    private final WarpManager warps;
    private final MessageManager messages;
    private final Map<UUID, BukkitTask> pending = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldown = new ConcurrentHashMap<>();
    private final Map<UUID, Location> origins = new ConcurrentHashMap<>();

    public TeleportManager(AnsamWarp plugin, WarpManager warps, MessageManager messages) { this.plugin=plugin; this.warps=warps; this.messages=messages; }

    public boolean isPending(Player p) { return pending.containsKey(p.getUniqueId()); }

    public long cooldownLeft(Player p) {
        long until=cooldown.getOrDefault(p.getUniqueId(),0L);
        return Math.max(0, (until-System.currentTimeMillis()+999)/1000);
    }

    public void start(Player p, String warpName, Location destination) {
        cancel(p, false);
        int delay=Math.max(0, plugin.getConfig().getInt("settings.teleport-delay-seconds",3));
        origins.put(p.getUniqueId(), p.getLocation().clone());
        p.sendMessage(messages.message("teleporting", Map.of("WARP", warpName)));
        play(p, "teleport-start");
        if (delay <= 0) { complete(p, destination); return; }
        final int[] remaining={delay};
        sendCountdown(p, remaining[0]);
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            remaining[0]--;
            if (remaining[0] > 0) sendCountdown(p, remaining[0]);
            else {
                BukkitTask t=pending.remove(p.getUniqueId());
                if (t!=null) t.cancel();
                origins.remove(p.getUniqueId());
                complete(p,destination);
            }
        }, 20L, 20L);
        pending.put(p.getUniqueId(), task);
    }

    private void sendCountdown(Player p,int sec) {
        p.sendMessage(messages.message("countdown", Map.of("SECONDS", String.valueOf(sec))));
        play(p,"countdown");
    }

    private void complete(Player p, Location destination) {
        if (!p.isOnline()) return;
        boolean ok=p.teleport(destination);
        if (ok) { cooldown.put(p.getUniqueId(), System.currentTimeMillis()+plugin.getConfig().getLong("settings.cooldown-seconds",0)*1000L); play(p,"teleport-success"); }
    }

    public void cancel(Player p, boolean damage) {
        BukkitTask task=pending.remove(p.getUniqueId());
        if (task!=null) task.cancel();
        origins.remove(p.getUniqueId());
        if (task!=null) {
            p.sendMessage(messages.message(damage?"teleport-cancelled-damage":"teleport-cancelled", Map.of()));
            play(p,"teleport-cancel");
        }
    }

    @EventHandler public void onMove(PlayerMoveEvent e) {
        if (!plugin.getConfig().getBoolean("settings.cancel-on-move",true) || !isPending(e.getPlayer())) return;
        Location f=e.getFrom(), t=e.getTo();
        if (t!=null && (f.getX()!=t.getX() || f.getY()!=t.getY() || f.getZ()!=t.getZ())) cancel(e.getPlayer(),false);
    }
    @EventHandler public void onDamage(EntityDamageEvent e) { if (e.getEntity() instanceof Player p && plugin.getConfig().getBoolean("settings.cancel-on-damage",false)) cancel(p,true); }

    private void play(Player p,String node) {
        if (!plugin.getConfig().getBoolean("sounds.enabled",true)) return;
        String path="sounds."+node;
        try { Sound sound=Sound.valueOf(plugin.getConfig().getString(path+".sound","BLOCK_NOTE_BLOCK_PLING")); p.playSound(p.getLocation(),sound,(float)plugin.getConfig().getDouble(path+".volume",1.0),(float)plugin.getConfig().getDouble(path+".pitch",1.0)); } catch (IllegalArgumentException ignored) {}
    }
    public void cancelAll() { for(UUID id:new HashSet<>(pending.keySet())) { Player p=plugin.getServer().getPlayer(id); if(p!=null) cancel(p,false); else { BukkitTask t=pending.remove(id); if(t!=null)t.cancel(); } } }
}
