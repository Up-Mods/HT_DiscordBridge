package dev.upcraft.ht.discordbridge.model.player;

import org.jetbrains.annotations.Nullable;

public record PlayerDisconnectReason(Type type, @Nullable String message) {

    public enum Type {
        CLIENT_CRASH,
        CLIENT_DISCONNECT,
        SERVER_KICK,
        UNKNOWN;

        public boolean isClientReason() {
            return this == CLIENT_CRASH || this == CLIENT_DISCONNECT;
        }
    }
}


