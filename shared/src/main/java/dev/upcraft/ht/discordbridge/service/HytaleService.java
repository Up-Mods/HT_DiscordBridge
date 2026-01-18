package dev.upcraft.ht.discordbridge.service;

import dev.upcraft.ht.discordbridge.commands.WhitelistCommand;
import dev.upcraft.ht.discordbridge.model.discord.DiscordUser;
import dev.upcraft.ht.discordbridge.model.server.ServerStatus;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

public interface HytaleService {

    Instant getStartupTime();

    default Duration getUptime() {
        return Duration.between(getStartupTime(), Instant.now());
    }

    ServerStatus getServerStatus();

    int getOnlinePlayers();
    OptionalInt getMaxPlayers();

    CompletableFuture<Void> executeServerCommand(DiscordUser source, String command, @Nullable String commandMessageId);

    CompletableFuture<Void> onDiscordChat(DiscordUser user, String message);

    CompletableFuture<WhitelistCommand.Result> whitelistPlayer(DiscordUser source, String playerUsername, @Nullable String commandMessageId);
}
