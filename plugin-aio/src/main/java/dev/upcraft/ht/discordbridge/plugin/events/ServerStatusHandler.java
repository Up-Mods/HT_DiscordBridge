package dev.upcraft.ht.discordbridge.plugin.events;

import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.ShutdownEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import dev.upcraft.ht.discordbridge.util.Services;

public class ServerStatusHandler {

    public static void register(JavaPlugin plugin) {
        plugin.getEventRegistry().registerGlobal(BootEvent.class, _ -> Services.BOT.onServerStarted());
        plugin.getEventRegistry().registerGlobal(ShutdownEvent.class, _ -> Services.BOT.onServerShutdown().join());
    }
}
