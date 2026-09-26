package com.pheakdey86.ansamwarp;

import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public final class WarpCommand implements CommandExecutor, TabCompleter {
    private final AnsamWarp plugin; private final WarpManager warps; private final TeleportManager teleports; private final MessageManager messages;
    public WarpCommand(AnsamWarp plugin,WarpManager warps,TeleportManager teleports,MessageManager messages){this.plugin=plugin;this.warps=warps;this.teleports=teleports;this.messages=messages;}
    private void msg(CommandSender s,String key,Map<String,String> p){s.sendMessage(messages.message(key,p));}
    private boolean perm(CommandSender s,String p){if(!s.hasPermission(p)){msg(s,"no-permission",Map.of());return false;}return true;}
    @Override public boolean onCommand(CommandSender s,Command c,String label,String[] a){
        String n=c.getName().toLowerCase(Locale.ROOT);
        if(n.equals("ansamwarp")){if(!perm(s,"ansamwarp.admin"))return true;if(a.length==1&&a[0].equalsIgnoreCase("reload")){plugin.reloadPlugin();msg(s,"reload",Map.of());}else help(s);return true;}
        if(n.equals("setwarp")){if(!(s instanceof Player p)){msg(s,"player-only",Map.of());return true;}if(!perm(s,"ansamwarp.setwarp"))return true;if(a.length!=1){msg(s,"setwarp-usage",Map.of());return true;}String name=clean(a[0]);if(name==null){msg(s,"invalid-name",Map.of());return true;}boolean exists=warps.exists(name);warps.set(name,p.getLocation());msg(p,exists?"updated-warp":"setwarp",Map.of("WARP",name));return true;}
        if(n.equals("delwarp")){if(!perm(s,"ansamwarp.delwarp"))return true;if(a.length!=1){msg(s,"delwarp-usage",Map.of());return true;}String name=clean(a[0]);if(name==null){msg(s,"invalid-name",Map.of());return true;}if(!warps.remove(name)){msg(s,"warp-not-found",Map.of("WARP",name));return true;}msg(s,"delwarp",Map.of("WARP",name));return true;}
        if(!(s instanceof Player p)){msg(s,"player-only",Map.of());return true;}if(!perm(p,"ansamwarp.warp"))return true;
        if(a.length==0){if(!perm(p,"ansamwarp.warps"))return true;showWarps(p);return true;}
        String name=clean(a[0]);if(name==null||!warps.exists(name)){msg(p,"warp-not-found",Map.of("WARP",a[0]));return true;}
        if(plugin.getConfig().getBoolean("settings.require-per-warp-permission",false)&&!p.hasPermission("ansamwarp.warp."+name.toLowerCase(Locale.ROOT))){msg(p,"no-permission",Map.of());return true;}
        long left=teleports.cooldownLeft(p);if(left>0){msg(p,"cooldown",Map.of("SECONDS",String.valueOf(left)));return true;}
        Location dest=warps.get(name);if(dest==null){msg(p,"warp-not-found",Map.of("WARP",name));return true;}
        if(!plugin.getConfig().getBoolean("settings.cross-world",true)&&!p.getWorld().equals(dest.getWorld())){msg(p,"cross-world-disabled",Map.of());return true;}
        if(plugin.getConfig().getBoolean("settings.safe-teleport",true)&&!isSafe(dest)){msg(p,"unsafe",Map.of());return true;}
        teleports.start(p,name,dest);return true;
    }
    private void showWarps(Player p){List<String> list=warps.names();if(plugin.getConfig().getBoolean("settings.filter-warps-by-permission",true)&&plugin.getConfig().getBoolean("settings.require-per-warp-permission",false))list=list.stream().filter(w->p.hasPermission("ansamwarp.warp."+w.toLowerCase(Locale.ROOT))).toList();if(list.isEmpty()){msg(p,"no-warps",Map.of());return;}msg(p,"warps-header",Map.of("WARPS",String.join(", ",list)));}
    private boolean isSafe(Location l){if(l.getWorld()==null)return false; return l.getBlock().isPassable() && l.clone().add(0,1,0).getBlock().isPassable() && !l.clone().subtract(0,1,0).getBlock().isPassable();}
    private String clean(String s){if(s==null||s.isBlank()||s.length()>32||!s.matches("[A-Za-z0-9_-]+"))return null;return s.toLowerCase(Locale.ROOT);}
    private void help(CommandSender s){msg(s,"help",Map.of());}
    @Override public List<String> onTabComplete(CommandSender s,Command c,String a0,String[] a){String n=c.getName().toLowerCase(Locale.ROOT);if(a.length!=1)return List.of();String partial=a[0].toLowerCase(Locale.ROOT);if(n.equals("warp"))return warps.names().stream().filter(w->w.toLowerCase(Locale.ROOT).startsWith(partial)).collect(Collectors.toList());if(n.equals("setwarp")||n.equals("delwarp"))return warps.names().stream().filter(w->w.toLowerCase(Locale.ROOT).startsWith(partial)).collect(Collectors.toList());if(n.equals("ansamwarp"))return List.of("reload","help").stream().filter(x->x.startsWith(partial)).toList();return List.of();}
}
