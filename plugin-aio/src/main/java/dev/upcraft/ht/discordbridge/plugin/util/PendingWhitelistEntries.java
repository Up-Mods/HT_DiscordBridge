package dev.upcraft.ht.discordbridge.plugin.util;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupConnectEvent;
import com.hypixel.hytale.server.core.modules.accesscontrol.AccessControlModule;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleWhitelistProvider;
import com.hypixel.hytale.server.core.util.Config;
import dev.upcraft.ht.aspect.api.ReflectionHelper;
import dev.upcraft.ht.aspect.util.PlayerCache;
import dev.upcraft.ht.discordbridge.commands.WhitelistCommand;
import dev.upcraft.ht.discordbridge.model.player.PlayerInfo;
import dev.upcraft.ht.discordbridge.plugin.DiscordBridgeAIO;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class PendingWhitelistEntries {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClassFull();
    private static final Function<AccessControlModule, HytaleWhitelistProvider> builtinProvider = ReflectionHelper.getter(AccessControlModule.class, "whitelistProvider", HytaleWhitelistProvider.class);
    private final Config<PendingWhitelistEntries.Storage> config;

    private PendingWhitelistEntries(DiscordBridgeAIO plugin, Config<PendingWhitelistEntries.Storage> config) {
        this.config = config;
    }

    public void onSetup(DiscordBridgeAIO plugin) {
        plugin.getEventRegistry().registerGlobal((short) -1, PlayerSetupConnectEvent.class, event -> {
            var id = event.getUuid();
            var name = event.getUsername();

            synchronized (config) {
                var pending = config.get();
                if(pending.players.remove(name)) {
                    LOGGER.atInfo().log("Adding pending player %s to whitelist", name);
                    var whitelist = getBuiltinWhitelist();
                    whitelist.modify(uuids -> uuids.add(id));
                    config.save();
                }
            }
        });
    }

    public CompletableFuture<Boolean> modifyPendingPlayers(Predicate<Set<String>> action) {
        return config.load()
                .thenApply(storage -> action.test(storage.players))
                .thenComposeAsync(modified -> modified ? config.save().thenApply(_ -> true) : CompletableFuture.completedFuture(false));
    }

    public static CompletableFuture<WhitelistCommand.Result> addPlayerToWhitelist(@Nullable UUID playerId, @Nullable String playerUsername) {
        // TODO rewrite to first try and resolve the profile!
        if(playerId != null) {
            var playerInfo = new PlayerInfo(playerId, playerUsername, Instant.MIN);
            var whitelist = getBuiltinWhitelist();
            if(whitelist.modify(uuids -> uuids.add(playerId))) {
                return CompletableFuture.completedFuture(new WhitelistCommand.Result(WhitelistCommand.ResultType.SUCCESS, playerInfo));
            }
            else {
                return CompletableFuture.completedFuture(new WhitelistCommand.Result(WhitelistCommand.ResultType.ALREADY_WHITELISTED, playerInfo));
            }
        }

        return DiscordBridgeAIO.getInstance().getPendingWhitelistEntries().modifyPendingPlayers(strings -> strings.add(playerUsername))
                .thenCompose(result -> PlayerCache.get().getProfileForUsername(playerUsername)
                            .thenApply(profile -> new WhitelistCommand.Result(
                                    result ? WhitelistCommand.ResultType.SUCCESS
                                            : WhitelistCommand.ResultType.ALREADY_PENDING,
                                    Convert.toPlayerInfo(profile)
                                    )
                            )
                );
    }

    @SuppressWarnings("unchecked")
    public static <T> PendingWhitelistEntries create(DiscordBridgeAIO plugin, BiFunction<String, BuilderCodec<T>, Config<T>> configStore) {
        var config = (Config<Storage>) configStore.apply("pending_whitelist_entries", (BuilderCodec<T>) Storage.CODEC);
        return new PendingWhitelistEntries(plugin, config);
    }

    private static HytaleWhitelistProvider getBuiltinWhitelist() {
        return builtinProvider.apply(AccessControlModule.get());
    }

    public static class Storage {

        public static final BuilderCodec<Storage> CODEC = BuilderCodec.builder(Storage.class, Storage::new)
                .append(new KeyedCodec<>("PendingPlayers", Codec.STRING_ARRAY),
                        (instance, value, extra) -> {
                            instance.players.clear();
                            instance.players.addAll(Arrays.asList(value));
                        },
                        (instance, extra) -> instance.players.toArray(String[]::new)
                )
                .add()
                .build();

        private final Set<String> players = new HashSet<>();
    }
}
