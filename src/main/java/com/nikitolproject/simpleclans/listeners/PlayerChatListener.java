package com.nikitolproject.simpleclans.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import com.nikitolproject.simpleclans.ClanPlugin;

public class PlayerChatListener implements Listener {

    private final ClanPlugin plugin;

    public PlayerChatListener(ClanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.getClanManager().isClanChatToggled(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getClanManager().sendClanMessage(player, event.getMessage());
        }
    }
}
