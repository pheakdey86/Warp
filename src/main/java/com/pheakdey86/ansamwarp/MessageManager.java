package com.pheakdey86.ansamwarp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public final class MessageManager {
    private final AnsamWarp plugin;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private boolean smallCaps;
    private String prefix;
    private String gradientStart;
    private String gradientEnd;

    private static final Map<Character, String> SMALL = new HashMap<>();
    static {
        String normal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String small  = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i=0;i<26;i++) SMALL.put(normal.charAt(i), String.valueOf(small.charAt(i)));
        for (int i=0;i<26;i++) SMALL.put(normal.charAt(i+26), String.valueOf(small.charAt(i)));
    }

    public MessageManager(AnsamWarp plugin) { this.plugin = plugin; reload(); }

    public void reload() {
        FileConfiguration c = plugin.getConfig();
        smallCaps = c.getBoolean("small_cap", true);
        prefix = c.getString("prefix", "");
        gradientStart = c.getString("gradient-start", "#00f5ff");
        gradientEnd = c.getString("gradient-end", "#5ae78e");
    }

    public Component message(String key, Map<String,String> placeholders) {
        String raw = plugin.getConfig().getString("messages." + key, "");
        for (var e : placeholders.entrySet()) raw = raw.replace("{" + e.getKey() + "}", e.getValue());
        return parse(prefix + raw);
    }

    public Component messageNoPrefix(String key, Map<String,String> placeholders) {
        String raw = plugin.getConfig().getString("messages." + key, "");
        for (var e : placeholders.entrySet()) raw = raw.replace("{" + e.getKey() + "}", e.getValue());
        return parse(raw);
    }

    public Component parse(String raw) {
        raw = translateTags(raw);
        if (smallCaps) raw = smallCaps(raw);
        return mini.deserialize(raw);
    }

    private String translateTags(String s) {
        s = s.replace("[gradient]", "<gradient:" + gradientStart + "," + gradientEnd + ">")
             .replace("[/gradient]", "</gradient>")
             .replace("[green]", "<green>").replace("[/green]", "</green>")
             .replace("[red]", "<red>").replace("[/red]", "</red>")
             .replace("[yellow]", "<yellow>").replace("[/yellow]", "</yellow>")
             .replace("[aqua]", "<aqua>").replace("[/aqua]", "</aqua>")
             .replace("[white]", "<white>").replace("[/white]", "</white>")
             .replace("[gray]", "<gray>").replace("[/gray]", "</gray>");
        s = s.replaceAll("&#([A-Fa-f0-9]{6})", "<#$1>");
        StringBuilder b = new StringBuilder();
        for (int i=0;i<s.length();i++) {
            char ch=s.charAt(i);
            if (ch=='&' && i+1<s.length()) {
                char n=s.charAt(++i);
                String tag=switch(Character.toLowerCase(n)) {
                    case '0'->"<black>"; case '1'->"<dark_blue>"; case '2'->"<dark_green>"; case '3'->"<dark_aqua>";
                    case '4'->"<dark_red>"; case '5'->"<dark_purple>"; case '6'->"<gold>"; case '7'->"<gray>";
                    case '8'->"<dark_gray>"; case '9'->"<blue>"; case 'a'->"<green>"; case 'b'->"<aqua>";
                    case 'c'->"<red>"; case 'd'->"<light_purple>"; case 'e'->"<yellow>"; case 'f'->"<white>";
                    case 'l'->"<bold>"; case 'm'->"<strikethrough>"; case 'n'->"<underlined>"; case 'o'->"<italic>"; case 'r'->"<reset>";
                    default -> "&"+n;
                }; b.append(tag);
            } else b.append(ch);
        }
        return b.toString();
    }

    private String smallCaps(String s) {
        StringBuilder out = new StringBuilder();
        boolean inTag=false;
        for (int i=0;i<s.length();i++) {
            char ch=s.charAt(i);
            if (ch=='<') inTag=true;
            if (inTag) out.append(ch); else out.append(SMALL.getOrDefault(ch, String.valueOf(ch)));
            if (ch=='>') inTag=false;
        }
        return out.toString();
    }
}
