package com.nikitolproject.simpleclans.listeners;

import com.nikitolproject.simpleclans.ClanPlugin;
import com.nikitolproject.simpleclans.models.Clan;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageListener implements Listener {

    private final ClanPlugin plugin;

    public PlayerDamageListener(ClanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (plugin.getConfigManager().isFriendlyFireEnabled()) {
            return;
        }

        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        Clan attackerClan = plugin.getClanManager().getClan(attacker.getUniqueId());
        Clan victimClan = plugin.getClanManager().getClan(victim.getUniqueId());

        if (attackerClan != null && attackerClan.equals(victimClan)) {
            event.setCancelled(true);
            attacker.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("cant-hurt-member")));
        }
    }
}
