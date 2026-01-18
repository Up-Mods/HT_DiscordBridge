package dev.upcraft.ht.discordbridge.commands;

import dev.upcraft.ht.discordbridge.model.player.PartialPlayerInfo;
import org.jspecify.annotations.Nullable;

public interface WhitelistCommand {

    enum ResultType {
        SUCCESS,
        ALREADY_PENDING,
        ALREADY_WHITELISTED,
    }

    record Result(ResultType type, @Nullable PartialPlayerInfo playerInfo) {}
}
