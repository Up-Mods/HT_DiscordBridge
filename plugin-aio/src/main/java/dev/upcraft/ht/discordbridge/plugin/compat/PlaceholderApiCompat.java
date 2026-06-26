package dev.upcraft.ht.discordbridge.plugin.compat;

import at.helpch.placeholderapi.PlaceholderAPI;
import dev.upcraft.ht.discordbridge.plugin.service.AIOHytaleService;

public class PlaceholderApiCompat {

    public static void registerProcessors(AIOHytaleService service) {
        service.registerMessageProcessor(PlaceholderAPI::setPlaceholders);
    }
}
