package dev.upcraft.ht.discordbridge.discord.extensions.server_status

import dev.kord.core.event.gateway.ReadyEvent
import dev.kordex.core.extensions.event
import dev.upcraft.ht.discordbridge.discord.extensions.DiscordBridgeExtension
import dev.upcraft.ht.discordbridge.discord.util.StartupConfig
import dev.upcraft.ht.discordbridge.discord.util.logger

// TODO translations
class ServerStatusExtension(cfg: StartupConfig) : DiscordBridgeExtension(cfg) {
    override val name = "server_status"

    companion object {
        var INSTANCE: ServerStatusExtension? = null
    }

    init {
        INSTANCE = this
    }

    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                logger.info { "Connected to discord as ${event.self.tag}" }
            }
        }
    }
}
