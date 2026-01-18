package dev.upcraft.ht.discordbridge.plugin.events;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import dev.upcraft.ht.discordbridge.model.player.PlayerInfo;
import dev.upcraft.ht.discordbridge.plugin.util.Convert;
import dev.upcraft.ht.discordbridge.util.Services;

import java.time.Instant;

public class PlayerCountHandler {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void register(JavaPlugin plugin) {
        plugin.getEventRegistry().register(PlayerConnectEvent.class, event -> {
            // TODO better track join times
            var joinTime = Instant.now();

            var ref = event.getPlayerRef();
            var info = new PlayerInfo(ref.getUuid(), ref.getUsername(), joinTime);

            LOGGER.atInfo().log("Player %s joined the server!", info);

            Services.BOT.onPlayerJoin(info);
        });

        plugin.getEventRegistry().register(PlayerDisconnectEvent.class, event -> {
            var player = Convert.toPlayerInfo(event.getPlayerRef());
            var reason = Convert.toReason(event.getDisconnectReason());

            switch (reason.type()) {
                case SERVER_KICK -> LOGGER.atInfo().log("Player %s was kicked from the server: %s", player, reason.message());
                case CLIENT_DISCONNECT -> LOGGER.atInfo().log("Player %s disconnected from server.", player);
                default -> LOGGER.atInfo().log("Player %s lost connection to the server!", player);
            }

            // todo include session duration
            Services.BOT.onPlayerLeave(player, reason);
        });
    }
}
