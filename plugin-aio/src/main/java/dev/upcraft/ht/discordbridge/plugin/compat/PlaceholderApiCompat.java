package dev.upcraft.ht.discordbridge.plugin.compat;

import at.helpch.placeholderapi.PlaceholderAPI;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import dev.upcraft.ht.discordbridge.plugin.service.AIOHytaleService;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.BiFunction;

public class PlaceholderApiCompat {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void registerProcessors(AIOHytaleService service, JavaPlugin plugin) {
        try {
            var apiClass = plugin.getClassLoader().loadClass(PlaceholderAPI.class.getName());
            var handle = MethodHandles.publicLookup().findStatic(apiClass, "setPlaceholders", MethodType.methodType(String.class, PlayerRef.class, String.class));
            BiFunction<@Nullable PlayerRef, String, String> processor = (playerRef, message) -> {
                try {
                    return (String) handle.invokeExact(playerRef, message);
                } catch (Throwable e) {
                    throw new RuntimeException("Reflection error", e);
                }
            };
            service.registerMessageProcessor(processor);
        } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            LOGGER.atWarning().withCause(e).log("Unable to register PlaceholderAPI integration");
        }
    }
}
