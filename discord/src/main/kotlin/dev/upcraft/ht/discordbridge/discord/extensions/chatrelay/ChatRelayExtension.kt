package dev.upcraft.ht.discordbridge.discord.extensions.chatrelay

import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.gateway.Intent
import dev.kord.gateway.Intent.GuildMembers
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import dev.kordex.core.checks.isNotBot
import dev.kordex.core.checks.isNotInThread
import dev.kordex.core.extensions.event
import dev.upcraft.ht.discordbridge.discord.extensions.DiscordBridgeExtension
import dev.upcraft.ht.discordbridge.discord.i18n.Translations
import dev.upcraft.ht.discordbridge.discord.util.Hytale
import dev.upcraft.ht.discordbridge.discord.util.StartupConfig
import dev.upcraft.ht.discordbridge.discord.util.addMemberContext
import dev.upcraft.ht.discordbridge.discord.util.asDiscordUser

class ChatRelayExtension(cfg: StartupConfig) : DiscordBridgeExtension(cfg) {
    override val name = "chat_relay"

    @OptIn(PrivilegedIntent::class)
    override val intents = mutableSetOf(Intent.GuildMessages, Intent.MessageContent)

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

                    val formattedMessage = Translations.Hytale.chatMessage
                        .translateNamed(buildMap {
                            addMemberContext(it)
                            put("message.content", event.message.content)
                        })

                    Hytale.onDiscordChat(discordUser, formattedMessage)
                }
            }
        }
    }
}
