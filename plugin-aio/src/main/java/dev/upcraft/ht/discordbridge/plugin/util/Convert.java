package dev.upcraft.ht.discordbridge.plugin.util;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.upcraft.ht.aspect.api.auth.PlayerGameProfile;
import dev.upcraft.ht.discordbridge.model.player.PartialPlayerInfo;
import dev.upcraft.ht.discordbridge.model.player.PlayerDisconnectReason;
import dev.upcraft.ht.discordbridge.model.player.PlayerInfo;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class Convert {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static PlayerDisconnectReason toReason(PacketHandler.DisconnectReason reason) {
        if(reason.getServerDisconnectReason() != null) {
            return new PlayerDisconnectReason(PlayerDisconnectReason.Type.SERVER_KICK, reason.getServerDisconnectReason());
        }

        if(reason.getClientDisconnectType() != null) {
            var reasonType = switch (reason.getClientDisconnectType()) {
                case Crash -> PlayerDisconnectReason.Type.CLIENT_CRASH;
                case Disconnect -> PlayerDisconnectReason.Type.CLIENT_DISCONNECT;
                //noinspection UnnecessaryDefault
                default -> {
                    LOGGER.atSevere().log("Unknown disconnect type: %s", reason.getClientDisconnectType());
                    yield PlayerDisconnectReason.Type.UNKNOWN;
                }
            };

            return new PlayerDisconnectReason(reasonType, null);
        }

        return new PlayerDisconnectReason(PlayerDisconnectReason.Type.UNKNOWN, null);
    }

    public static PartialPlayerInfo toPlayerInfo(PlayerRef player) {
        // TODO get join time
        return new PlayerInfo(player.getUuid(), player.getUsername(), Instant.MIN);
    }

    @Nullable
    public static PartialPlayerInfo toPlayerInfo(PlayerGameProfile profile) {
        if(!profile.isComplete()) {
            return null;
        }
        // TODO get join time
        return new PlayerInfo(profile.id().orElseThrow(), profile.username().orElseThrow(), Instant.MIN);
    }
}
