package dev.upcraft.ht.discordbridge.discord.extensions.chatrelay

import dev.kord.core.event.message.MessageCreateEvent
import dev.kordex.core.checks.isNotBot
import dev.kordex.core.checks.isNotInThread
import dev.kordex.core.extensions.event
import dev.upcraft.ht.discordbridge.discord.extensions.DiscordBridgeExtension
import dev.upcraft.ht.discordbridge.discord.util.Hytale
import dev.upcraft.ht.discordbridge.discord.util.StartupConfig
import dev.upcraft.ht.discordbridge.discord.util.asDiscordUser

class ChatRelayExtension(cfg: StartupConfig) : DiscordBridgeExtension(cfg) {
    override val name = "chat_relay"

    override suspend fun setup() {

        event<MessageCreateEvent> {
            check {
                isNotBot()
                isNotInThread()
                failIf { event.message.channelId != cfg.channels.chatRelayChannel }
                failIf { event.message.content.startsWith(bot.settings.chatCommandsBuilder.defaultPrefix) }
            }

            action {
                event.member?.let {
                    val discordUser = it.asDiscordUser()
                    // TODO fancier format
                    Hytale.onDiscordChat(discordUser, event.message.content)
                }
            }
        }
    }
}
