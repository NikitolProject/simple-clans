package com.nikitolproject.simpleclans;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import com.nikitolproject.simpleclans.commands.ClanCommand;
import com.nikitolproject.simpleclans.config.ConfigManager;
import com.nikitolproject.simpleclans.database.DatabaseManager;
import com.nikitolproject.simpleclans.integrations.LuckPermsHook;
import com.nikitolproject.simpleclans.integrations.PlaceholderAPIHook;
import com.nikitolproject.simpleclans.listeners.PlayerChatListener;
import com.nikitolproject.simpleclans.listeners.PlayerDamageListener;
import com.nikitolproject.simpleclans.listeners.PlayerJoinListener;
import com.nikitolproject.simpleclans.managers.ClanManager;

import java.sql.SQLException;

public class ClanPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private ClanManager clanManager;
    private LuckPermsHook luckPermsHook;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.connect();
        } catch (SQLException e) {
            getLogger().severe("Failed to connect to database!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        clanManager = new ClanManager(this);
        clanManager.loadClans();

        luckPermsHook = new LuckPermsHook();

        getServer().getCommandMap().register("simpleclans", new ClanCommand(this));

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
        }

        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(this), this);

        getLogger().info("Simple Clans enabled!");
    }

    @Override
    public void onDisable() {
        try {
            databaseManager.close();
        } catch (SQLException e) {
            getLogger().severe("Failed to close database connection!");
            e.printStackTrace();
        }
        getLogger().info("Simple Clans disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
