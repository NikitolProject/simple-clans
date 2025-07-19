package com.nikitolproject.simpleclans.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import com.nikitolproject.simpleclans.ClanPlugin;
import com.nikitolproject.simpleclans.models.Clan;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final ClanPlugin plugin;

    public PlaceholderAPIHook(ClanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "clan";
    }

    @Override
    public String getAuthor() {
        return "NikitolProject";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // Required for placeholders that don't change often
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("name")) {
            Clan clan = plugin.getClanManager().getClan(player.getUniqueId());
            if (clan != null) {
                String formattedTag = plugin.getConfigManager().getClanTagFormat().replace("%clan_name%", clan.getName());
                return ClanPlugin.colorize(formattedTag) + org.bukkit.ChatColor.RESET;
            }
            return "";
        }

        return null;
    }
}
