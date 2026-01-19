package dev.upcraft.ht.discordbridge.service;

import dev.upcraft.ht.discordbridge.model.player.PartialPlayerInfo;
import dev.upcraft.ht.discordbridge.model.player.PlayerDisconnectReason;
import dev.upcraft.ht.discordbridge.model.player.PlayerInfo;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface BotService {

    @Deprecated
    void setStartupComplete();

    CompletableFuture<Void> botStartup();

    void onPlayerJoin(PlayerInfo player);

    void onPlayerLeave(PartialPlayerInfo player, PlayerDisconnectReason reason);

    void onServerStarted();

    // needs to be a future or else there's not enough time to get our message through
    CompletableFuture<Void> onServerShutdown();

    void onPlayerChat(PartialPlayerInfo player, String message);

    void sendCommandFeedback(String message, @Nullable String originalMessageId);
}
