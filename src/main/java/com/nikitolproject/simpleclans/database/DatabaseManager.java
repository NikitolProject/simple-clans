package com.nikitolproject.simpleclans.database;

import com.nikitolproject.simpleclans.ClanPlugin;
import com.nikitolproject.simpleclans.models.Clan;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {

    private final ClanPlugin plugin;
    private Connection connection;

    public DatabaseManager(ClanPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File dbFile = new File(dataFolder, plugin.getConfigManager().getDatabaseFile());
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        connection = DriverManager.getConnection(url);

        createTables();
    }

    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS clans ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "name TEXT NOT NULL UNIQUE, "
                    + "owner_uuid TEXT NOT NULL"
                    + ");");

            statement.execute("CREATE TABLE IF NOT EXISTS clan_members ("
                    + "clan_id INTEGER NOT NULL, "
                    + "player_uuid TEXT NOT NULL, "
                    + "FOREIGN KEY(clan_id) REFERENCES clans(id) ON DELETE CASCADE"
                    + ");");
        }
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public Clan createClan(String name, UUID owner) throws SQLException {
        String insertClanSQL = "INSERT INTO clans(name, owner_uuid) VALUES(?,?)";
        String insertMemberSQL = "INSERT INTO clan_members(clan_id, player_uuid) VALUES(?,?)";

        try (PreparedStatement clanStmt = connection.prepareStatement(insertClanSQL, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement memberStmt = connection.prepareStatement(insertMemberSQL)) {

            connection.setAutoCommit(false);

            clanStmt.setString(1, name);
            clanStmt.setString(2, owner.toString());
            clanStmt.executeUpdate();

            ResultSet generatedKeys = clanStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int clanId = generatedKeys.getInt(1);

                memberStmt.setInt(1, clanId);
                memberStmt.setString(2, owner.toString());
                memberStmt.executeUpdate();

                connection.commit();
                return new Clan(clanId, name, owner, new ArrayList<>(List.of(owner)));
            } else {
                connection.rollback();
                throw new SQLException("Creating clan failed, no ID obtained.");
            }
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void addMember(int clanId, UUID playerUuid) throws SQLException {
        String sql = "INSERT INTO clan_members(clan_id, player_uuid) VALUES(?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clanId);
            stmt.setString(2, playerUuid.toString());
            stmt.executeUpdate();
        }
    }

    public void removeMember(int clanId, UUID playerUuid) throws SQLException {
        String sql = "DELETE FROM clan_members WHERE clan_id = ? AND player_uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clanId);
            stmt.setString(2, playerUuid.toString());
            stmt.executeUpdate();
        }
    }

    public void deleteClan(int clanId) throws SQLException {
        String sql = "DELETE FROM clans WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clanId);
            stmt.executeUpdate();
        }
    }

    public List<Clan> getAllClans() throws SQLException {
        String clansSQL = "SELECT * FROM clans";
        String membersSQL = "SELECT player_uuid FROM clan_members WHERE clan_id = ?";
        List<Clan> clans = new ArrayList<>();

        try (Statement clanStmt = connection.createStatement();
             ResultSet clanRs = clanStmt.executeQuery(clansSQL)) {

            while (clanRs.next()) {
                int clanId = clanRs.getInt("id");
                String name = clanRs.getString("name");
                UUID owner = UUID.fromString(clanRs.getString("owner_uuid"));
                List<UUID> members = new ArrayList<>();

                try (PreparedStatement memberStmt = connection.prepareStatement(membersSQL)) {
                    memberStmt.setInt(1, clanId);
                    try (ResultSet memberRs = memberStmt.executeQuery()) {
                        while (memberRs.next()) {
                            members.add(UUID.fromString(memberRs.getString("player_uuid")));
                        }
                    }
                }
                clans.add(new Clan(clanId, name, owner, members));
            }
        }
        return clans;
    }
}
