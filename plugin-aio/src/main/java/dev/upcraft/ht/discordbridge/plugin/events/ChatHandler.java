package dev.upcraft.ht.discordbridge.plugin.events;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import dev.upcraft.ht.discordbridge.plugin.util.Convert;
import dev.upcraft.ht.discordbridge.util.Services;

public class ChatHandler {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void register(JavaPlugin plugin) {
        plugin.getEventRegistry().registerGlobal(PlayerChatEvent.class, event -> {

            var player = Convert.toPlayerInfo(event.getSender());
            Services.BOT.onPlayerChat(player, event.getContent());
        });
    }
}
