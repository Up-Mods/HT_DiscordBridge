package dev.upcraft.ht.discordbridge.util;

import dev.upcraft.ht.discordbridge.service.BotService;

import java.util.ServiceLoader;

public class Services {

    public static final BotService BOT = load(BotService.class);

    public static <T> T load(Class<T> service) {
        return ServiceLoader.load(service, Services.class.getClassLoader()).findFirst()
                .orElseThrow(() -> new IllegalStateException("No service found for %s".formatted(service.getName())));
    }
}
