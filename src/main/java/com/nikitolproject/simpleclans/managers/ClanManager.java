package com.nikitolproject.simpleclans.managers;

import com.nikitolproject.simpleclans.ClanPlugin;
import com.nikitolproject.simpleclans.models.Clan;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClanManager {

    private final ClanPlugin plugin;
    private final Map<String, Clan> clans = new HashMap<>();
    private final Map<UUID, String> playerClanMap = new HashMap<>();
    private final Map<UUID, List<String>> invites = new HashMap<>();
    private final Set<UUID> clanChatToggled = new HashSet<>();

    public ClanManager(ClanPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadClans() {
        try {
            for (Clan clan : plugin.getDatabaseManager().getAllClans()) {
                clans.put(clan.getName().toLowerCase(), clan);
                for (UUID memberUuid : clan.getMembers()) {
                    playerClanMap.put(memberUuid, clan.getName().toLowerCase());
                }
            }
            plugin.getLogger().info("Loaded " + clans.size() + " clans.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load clans from database!");
            e.printStackTrace();
        }
    }

    public Clan getClan(String name) {
        return clans.get(name.toLowerCase());
    }

    public Clan getClan(UUID playerUuid) {
        String clanName = playerClanMap.get(playerUuid);
        return clanName != null ? getClan(clanName) : null;
    }

    public void createClan(String name, UUID owner) {
        Player player = plugin.getServer().getPlayer(owner);
        if (player == null) {
            return;
        }

        if (!plugin.getLuckPermsHook().canCreateClan(player)) {
            player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        if (getClan(owner) != null) {
            player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("already-in-clan")));
            return;
        }

        int maxLen = plugin.getConfigManager().getMaxClanNameLength();
        if (name.length() > maxLen) {
            player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("clan-name-too-long").replace("%max_length%", String.valueOf(maxLen))));
            return;
        }

        if (getClan(name) != null) {
            player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("clan-name-taken")));
            return;
        }

        try {
            Clan clan = plugin.getDatabaseManager().createClan(name, owner);
            clans.put(name.toLowerCase(), clan);
            playerClanMap.put(owner, name.toLowerCase());
            player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("clan-created").replace("%clan_name%", name)));
        } catch (SQLException e) {
            player.sendMessage(ClanPlugin.colorize("&cAn error occurred while creating the clan."));
            e.printStackTrace();
        }
    }

    public void disbandClan(Player player) {
        Clan clan = getClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("not-in-clan")));
            return;
        }

        if (!clan.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        try {
            plugin.getDatabaseManager().deleteClan(clan.getId());

            for (UUID memberUuid : clan.getMembers()) {
                playerClanMap.remove(memberUuid);
                Player member = plugin.getServer().getPlayer(memberUuid);
                if (member != null) {
                    member.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("clan-disbanded").replace("%clan_name%", clan.getName())));
                }
            }
            clans.remove(clan.getName().toLowerCase());

        } catch (SQLException e) {
            player.sendMessage(ClanPlugin.colorize("&cAn error occurred while disbanding the clan."));
            e.printStackTrace();
        }
    }

    public void invitePlayer(Player inviter, String targetName) {
        Clan clan = getClan(inviter.getUniqueId());
        if (clan == null) {
            inviter.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("not-in-clan")));
            return;
        }

        if (!clan.getOwner().equals(inviter.getUniqueId())) {
            inviter.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            inviter.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("player-not-found")));
            return;
        }

        if (getClan(target.getUniqueId()) != null) {
            inviter.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("player-already-in-clan")));
            return;
        }

        if (clan.getMembers().size() >= plugin.getConfigManager().getMaxPlayers()) {
            inviter.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("clan-full")));
            return;
        }

        invites.computeIfAbsent(target.getUniqueId(), k -> new ArrayList<>()).add(clan.getName());
        inviter.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("player-invited").replace("%player_name%", target.getName())));

        TextComponent message = new TextComponent(ClanPlugin.colorize(plugin.getConfigManager().getMessage("invite-received").replace("%clan_name%", clan.getName())));
        
        TextComponent acceptButton = new TextComponent(ClanPlugin.colorize(" &a[Принять]"));
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan join " + clan.getName()));
        acceptButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ClanPlugin.colorize("&aНажмите, чтобы вступить в клан &f" + clan.getName())).create()));
        
        TextComponent denyButton = new TextComponent(ClanPlugin.colorize(" &c[Отклонить]"));
        denyButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan deny " + clan.getName()));
        denyButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ClanPlugin.colorize("&cНажмите, чтобы отклонить приглашение")).create()));

        message.addExtra(acceptButton);
        message.addExtra(denyButton);

        target.spigot().sendMessage(message);
    }

    public void joinClan(Player player, String clanName) {
        List<String> playerInvites = invites.get(player.getUniqueId());
        if (playerInvites == null || !playerInvites.contains(clanName)) {
            player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("no-invite")));
            return;
        }

        Clan clan = getClan(clanName);
        if (clan == null) {
            player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("invalid-clan")));
            return;
        }

        if (getClan(player.getUniqueId()) != null) {
            player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("already-in-clan")));
            return;
        }

        try {
            plugin.getDatabaseManager().addMember(clan.getId(), player.getUniqueId());
            clan.getMembers().add(player.getUniqueId());
            playerClanMap.put(player.getUniqueId(), clan.getName().toLowerCase());
            invites.remove(player.getUniqueId()); // Clear all invites after joining a clan

            for (UUID memberUuid : clan.getMembers()) {
                Player member = plugin.getServer().getPlayer(memberUuid);
                if (member != null) {
                    member.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("player-joined").replace("%player_name%", player.getName())));
                }
            }
        } catch (SQLException e) {
            player.sendMessage(ClanPlugin.colorize("&cAn error occurred while joining the clan."));
            e.printStackTrace();
        }
    }

    public void kickPlayer(Player kicker, String targetName) {
        Clan clan = getClan(kicker.getUniqueId());
        if (clan == null) {
            kicker.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("not-in-clan")));
            return;
        }

        if (!clan.getOwner().equals(kicker.getUniqueId())) {
            kicker.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("no-permission")));
            return;
        }

        OfflinePlayer target = null;
        UUID targetUuid = null;

        for (UUID memberUuid : clan.getMembers()) {
            OfflinePlayer offlineMember = Bukkit.getOfflinePlayer(memberUuid);
            if (offlineMember.getName() != null && offlineMember.getName().equalsIgnoreCase(targetName)) {
                target = offlineMember;
                targetUuid = memberUuid;
                break;
            }
        }

        if (target == null) {
            kicker.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("player-not-found-in-clan")));
            return;
        }

        if (target.getUniqueId().equals(kicker.getUniqueId())) {
            kicker.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("cannot-kick-self")));
            return;
        }

        try {
            plugin.getDatabaseManager().removeMember(clan.getId(), targetUuid);
            clan.getMembers().remove(targetUuid);
            playerClanMap.remove(targetUuid);

            kicker.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("player-kicked").replace("%player_name%", target.getName())));
            if (target.isOnline()) {
                target.getPlayer().sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("you-have-been-kicked").replace("%clan_name%", clan.getName())));
            }
        } catch (SQLException e) {
            kicker.sendMessage(ClanPlugin.colorize("&cAn error occurred while kicking the player."));
            e.printStackTrace();
        }
    }

    public boolean isClanChatToggled(UUID uuid) {
        return clanChatToggled.contains(uuid);
    }

    public void toggleClanChat(Player player) {
        if (isClanChatToggled(player.getUniqueId())) {
            clanChatToggled.remove(player.getUniqueId());
            player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("clan-chat-off")));
        } else {
            clanChatToggled.add(player.getUniqueId());
            player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("clan-chat-on")));
        }
    }

    public void sendClanMessage(Player sender, String message) {
        Clan clan = getClan(sender.getUniqueId());
        if (clan == null) {
            sender.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("not-in-clan")));
            return;
        }

        String formattedMessage = plugin.getConfigManager().getChatFormat()
                .replace("%player_name%", sender.getName())
                .replace("%message%", message);

        String finalMessage = ClanPlugin.colorize(formattedMessage);

        for (UUID memberUuid : clan.getMembers()) {
            Player member = plugin.getServer().getPlayer(memberUuid);
            if (member != null) {
                member.sendMessage(finalMessage);
            }
        }
    }

    public void listClanMembers(Player player) {
        Clan clan = getClan(player.getUniqueId());
        if (clan == null) {
            player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("not-in-clan")));
            return;
        }

        player.sendMessage(ClanPlugin.colorize("&6Members of " + clan.getName() + ":"));
        for (UUID memberUuid : clan.getMembers()) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(memberUuid);
            String memberName = member.getName();
            if (memberName == null) continue; // Should not happen, but good practice

            String status = member.isOnline() ? "&a(Online)" : "&c(Offline)";
            player.sendMessage(ClanPlugin.colorize("&7- " + memberName + " " + status));
        }
    }

    public void denyInvite(Player player, String clanName) {
        List<String> playerInvites = invites.get(player.getUniqueId());
        if (playerInvites != null) {
            if (playerInvites.remove(clanName)) {
                player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("invite-denied").replace("%clan_name%", clanName)));
            }
        }
    }

    public List<String> getInvites(UUID playerUuid) {
        return invites.get(playerUuid);
    }
}