package dev.upcraft.ht.discordbridge.plugin.service;

import com.google.auto.service.AutoService;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import dev.upcraft.ht.aspect.util.PlayerHelper;
import dev.upcraft.ht.discordbridge.commands.WhitelistCommand;
import dev.upcraft.ht.discordbridge.model.discord.DiscordUser;
import dev.upcraft.ht.discordbridge.model.server.ServerStatus;
import dev.upcraft.ht.discordbridge.plugin.DiscordBridgeAIO;
import dev.upcraft.ht.discordbridge.plugin.console.PluginConsoleSender;
import dev.upcraft.ht.discordbridge.plugin.util.PendingWhitelistEntries;
import dev.upcraft.ht.discordbridge.service.HytaleService;
import dev.upcraft.ht.discordbridge.util.Services;
import fi.sulku.hytale.TinyMsg;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@AutoService(HytaleService.class)
public class AIOHytaleService implements HytaleService {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final List<BiFunction<@Nullable PlayerRef, String, String>> messageProcessors = new ArrayList<>();

    public AIOHytaleService() {
        DiscordBridgeAIO.getInstance().initHytaleService(this);
    }

    @Override
    public Instant getStartupTime() {
        return HytaleServer.get().getBoot();
    }

    @Override
    public ServerStatus getServerStatus() {
        var server = HytaleServer.get();
        if(server.isShuttingDown()) {
            return ServerStatus.SHUTTING_DOWN;
        }

        if(server.isBooted()) {
            return ServerStatus.RUNNING;
        }

        return ServerStatus.STARTING;
    }

    @Override
    public int getOnlinePlayers() {
        return Universe.get().getPlayerCount();
    }

    @Override
    public OptionalInt getMaxPlayers() {
        // TODO mod? to disable player limit
        var config = HytaleServer.get().getConfig();
        return OptionalInt.of(config.getMaxPlayers());
    }

    @Override
    public CompletableFuture<Void> executeServerCommand(DiscordUser source, String command, @Nullable String commandMessageId) {
        LOGGER.atInfo().log("User %s (%s) executing console command: %s", source.displayName(), source.userId(), command);
        var sender = new PluginConsoleSender(ConsoleSender.INSTANCE, commandMessageId);
        return CommandManager.get().handleCommand(sender, command).exceptionally(throwable -> {
            Services.BOT.sendCommandFeedback("ERROR: %s".formatted(throwable.getMessage()), commandMessageId);
            LOGGER.atSevere().withCause(throwable).log("Unable to process command: %s", command);
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> onDiscordChat(DiscordUser user, String message) {
        return CompletableFuture.runAsync(() -> PlayerHelper.broadcastMessageToAllPlayers(TinyMsg.parse(message)));
    }

    @Override
    public CompletableFuture<WhitelistCommand.Result> whitelistPlayer(DiscordUser source, String usernameOrUUID, @Nullable String commandMessageId) {
        LOGGER.atInfo().log("User %s (%s) whitelisting player: %s", source.displayName(), source.userId(), usernameOrUUID);

        //TODO remove all of this

        UUID playerUUID;
        String playerUsername;
        try {
            playerUUID = UUID.fromString(usernameOrUUID);
            playerUsername = null;
        } catch (IllegalArgumentException ignore) {
            // not a valid ID, assume name
            playerUUID = null;
            playerUsername = usernameOrUUID;
        }

        // check online players
        if (playerUUID == null) {
            PlayerRef player = Universe.get().getPlayerByUsername(playerUsername, NameMatching.EXACT);
            if (player != null) {
                playerUUID = player.getUuid();
            }
        }

        // fallback: assume username and store for next join
        //else: just add to whitelist.
        return PendingWhitelistEntries.addPlayerToWhitelist(playerUUID, playerUsername);
    }

    @Override
    public CompletableFuture<String> applyReplacements(@Nullable UUID playerId, String message) {
        if(messageProcessors.isEmpty()) {
            return CompletableFuture.completedFuture(message);
        }

        var player = playerId != null ? Universe.get().getPlayer(playerId) : null;
        var contextWorld = player != null && player.getWorldUuid() != null
                ? Objects.requireNonNull(Universe.get().getWorld(player.getWorldUuid()), () -> "World %s not found for player %s (%s)".formatted(player.getWorldUuid(), player.getUsername(), player.getUuid()))
                : Universe.get().getDefaultWorld();

        return CompletableFuture.supplyAsync(() -> {
            var formattedMessage = message;
            for (BiFunction<PlayerRef, String, String> messageProcessor : messageProcessors) {
                formattedMessage = messageProcessor.apply(player, formattedMessage);
            }

            return formattedMessage;
        }, contextWorld);
    }

    public void registerMessageProcessor(BiFunction<@Nullable PlayerRef, String, String> processor) {
        messageProcessors.add(processor);
    }
}
