package com.nikitolproject.simpleclans.models;

import java.util.List;
import java.util.UUID;

public class Clan {

    private final int id;
    private final String name;
    private final UUID owner;
    private final List<UUID> members;

    public Clan(int id, String name, UUID owner, List<UUID> members) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.members = members;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public List<UUID> getMembers() {
        return members;
    }
}
