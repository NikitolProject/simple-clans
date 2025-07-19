package com.nikitolproject.simpleclans.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.nikitolproject.simpleclans.ClanPlugin;
import com.nikitolproject.simpleclans.models.Clan;

public class PlayerJoinListener implements Listener {

    private final ClanPlugin plugin;

    public PlayerJoinListener(ClanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Clan clan = plugin.getClanManager().getClan(player.getUniqueId());
        if (clan != null) {
            // Optional: You can add a welcome message here
        }
    }
}
