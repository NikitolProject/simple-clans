package com.nikitolproject.simpleclans.commands;

import com.nikitolproject.simpleclans.ClanPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClanCommand extends Command {

    private final ClanPlugin plugin;

    public ClanCommand(ClanPlugin plugin) {
        super("clan");
        this.plugin = plugin;
        this.setDescription("Основная команда клана");
        this.setUsage("/clan <subcommand> [args]");
        this.setPermission("simpleclans.use");
        this.setAliases(List.of("c"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ClanPlugin.colorize("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Show help message
            player.sendMessage(ClanPlugin.colorize("&cUsage: /clan <create|invite|join|kick|list|disband|chat>"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("invalid-args")));
                    return true;
                }
                String clanName = args[1];
                plugin.getClanManager().createClan(clanName, player.getUniqueId());
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("invalid-args")));
                    return true;
                }
                String targetName = args[1];
                plugin.getClanManager().invitePlayer(player, targetName);
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("invalid-args")));
                    return true;
                }
                String clanToJoin = args[1];
                plugin.getClanManager().joinClan(player, clanToJoin);
                break;
            case "kick":
                if (args.length < 2) {
                    player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("invalid-args")));
                    return true;
                }
                String targetToKick = args[1];
                plugin.getClanManager().kickPlayer(player, targetToKick);
                break;
            case "chat":
                if (args.length == 1) {
                    plugin.getClanManager().toggleClanChat(player);
                    return true;
                }
                String chatAction = args[1].toLowerCase();
                if (chatAction.equals("on") || chatAction.equals("off")) {
                    plugin.getClanManager().toggleClanChat(player);
                } else {
                    String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                    plugin.getClanManager().sendClanMessage(player, message);
                }
                break;
            case "list":
                plugin.getClanManager().listClanMembers(player);
                break;
            case "disband":
                plugin.getClanManager().disbandClan(player);
                break;
            case "deny":
                if (args.length < 2) {
                    player.sendMessage(ClanPlugin.colorize(plugin.getConfigManager().getMessage("invalid-args")));
                    return true;
                }
                String clanToDeny = args[1];
                plugin.getClanManager().denyInvite(player, clanToDeny);
                break;
            default:
                player.sendMessage(ClanPlugin.colorize("&cUnknown subcommand. Usage: /clan <create|invite|join|kick|list|disband|chat>"));
                break;
        }

        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], 
                Arrays.asList("create", "invite", "join", "kick", "list", "disband", "chat", "deny"), 
                new ArrayList<>());
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("invite") || args[0].equalsIgnoreCase("kick")) {
                return StringUtil.copyPartialMatches(args[1], 
                    Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), 
                    new ArrayList<>());
            }
            if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("deny")) {
                List<String> invites = plugin.getClanManager().getInvites(((Player) sender).getUniqueId());
                if (invites != null) {
                    return StringUtil.copyPartialMatches(args[1], invites, new ArrayList<>());
                }
            }
        }

        return Collections.emptyList();
    }
}