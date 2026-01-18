package dev.upcraft.ht.discordbridge.model.player;

import java.time.Instant;
import java.util.UUID;

public record PlayerInfo(UUID uuid, String displayName, Instant joinedAt) implements PartialPlayerInfo {

    @Override
    public String toString() {
        return String.format("%s (%s)", uuid, displayName);
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
