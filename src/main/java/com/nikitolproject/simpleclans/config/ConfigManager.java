package com.nikitolproject.simpleclans.config;

import org.bukkit.configuration.file.FileConfiguration;
import com.nikitolproject.simpleclans.ClanPlugin;

import java.util.List;

public class ConfigManager {

    private final ClanPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(ClanPlugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        this.config = this.plugin.getConfig();
    }

    public String getDatabaseFile() {
        return config.getString("database.file", "clans.db");
    }

    public int getMaxPlayers() {
        return config.getInt("clan.max-players", 15);
    }

    public int getMaxClanNameLength() {
        return config.getInt("clan.max-name-length", 16);
    }

    public boolean isFriendlyFireEnabled() {
        return config.getBoolean("clan.friendly-fire", false);
    }

    public String getClanTagFormat() {
        return config.getString("clan.tag-format", "&7[%clan_name%&7]");
    }

    public String getChatFormat() {
        return config.getString("chat.format", "&8[&bКлан&8] [%luckperms_prefix%] %player_name%: &f%message%");
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "");
    }
}
