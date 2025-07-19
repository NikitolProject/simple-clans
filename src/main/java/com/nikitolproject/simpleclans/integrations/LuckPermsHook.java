package com.nikitolproject.simpleclans.integrations;

import org.bukkit.entity.Player;

public class LuckPermsHook {

    public boolean canCreateClan(Player player) {
        return player.hasPermission("simpleclans.create");
    }
}
