package dev.upcraft.ht.discordbridge.plugin;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.ShutdownReason;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import dev.upcraft.ht.aspect.util.Env;
import dev.upcraft.ht.discordbridge.discord.HtDiscordBot;
import dev.upcraft.ht.discordbridge.discord.util.StartupConfig;
import dev.upcraft.ht.discordbridge.plugin.config.BotConfigCodecs;
import dev.upcraft.ht.discordbridge.plugin.events.ChatHandler;
import dev.upcraft.ht.discordbridge.plugin.events.PlayerCountHandler;
import dev.upcraft.ht.discordbridge.plugin.events.ServerStatusHandler;
import dev.upcraft.ht.discordbridge.plugin.util.PendingWhitelistEntries;
import dev.upcraft.ht.discordbridge.util.Services;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;


public class DiscordBridgeAIO extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static volatile @Nullable DiscordBridgeAIO instance;

    private final @Nullable Config<StartupConfig> botConfig;
    private final boolean hasValidStart;

    private final PendingWhitelistEntries pendingWhitelistEntries;

    @Nullable
    private volatile CompletableFuture<Void> bot;

    public DiscordBridgeAIO(JavaPluginInit init) {
        super(init);
        instance = this;
        LOGGER.atInfo().log("Hello from %s version %s", this.getName(), this.getManifest().getVersion().toString());

        if(Env.get(StartupConfig.BOT_TOKEN_KEY).filter(s -> !s.isBlank()).isPresent()) {
            LOGGER.atInfo().log("Environment variables config detected, skipping config file generation.");
            this.botConfig = null;
            this.hasValidStart = true;
        }
        else {
            var configName = "DiscordBridge";
            this.hasValidStart = Files.exists(this.getDataDirectory().resolve("%s.json".formatted(configName)));
            this.botConfig = this.withConfig(configName, BotConfigCodecs.STARTUP_CODEC);
        }
        this.pendingWhitelistEntries = PendingWhitelistEntries.create(this, this::withConfig);
    }

    public static DiscordBridgeAIO getInstance() {
        return Objects.requireNonNull(instance, "MOD INSTANCE NOT INITIALIZED!");
    }

    @Override
    protected void setup() {
        instance = this;
        this.pendingWhitelistEntries.onSetup(this);
        if(hasValidStart) {
            ServerStatusHandler.register(this);
            PlayerCountHandler.register(this);
            ChatHandler.register(this);
        }
    }

    @Override
    protected void start() {
        instance = this;

        StartupConfig startupConfig;
        if(botConfig != null) {
            botConfig.save().join();
            startupConfig = botConfig.get();

            if(!hasValidStart || startupConfig.getToken().isBlank() || startupConfig.getToken().equals(StartupConfig.BOT_TOKEN_DEFAULT_VALUE)) {
                LOGGER.atWarning().log("PLEASE FILL IN THE DISCORD BRIDGE CONFIG AND RESTART YOUR SERVER");
                return;
            }
        }
        else {
            startupConfig = StartupConfig.fromEnv();
        }

        bot = HtDiscordBot.startAsync(startupConfig).exceptionally(throwable -> {
            LOGGER.atSevere().withCause(throwable).log("Discord bot error");
            HytaleServer.get().shutdownServer(ShutdownReason.VALIDATE_ERROR.withMessage("Discord bot error: " + throwable.getMessage()));
            return null;
        });

        Services.BOT.botStartup().join();
    }

    @Override
    protected void shutdown() {
        // TODO properly stop the bot
        var botCopy = bot;
        if(botCopy != null && !botCopy.isDone()) {
            botCopy.cancel(true);
        }
        bot = null;
        instance = null;
    }

    public PendingWhitelistEntries getPendingWhitelistEntries() {
        return pendingWhitelistEntries;
    }
}
