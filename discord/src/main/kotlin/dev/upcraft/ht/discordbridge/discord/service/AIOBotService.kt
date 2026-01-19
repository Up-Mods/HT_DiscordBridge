package dev.upcraft.ht.discordbridge.discord.service

import com.google.auto.service.AutoService
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.reply
import dev.kord.rest.builder.message.allowedMentions
import dev.upcraft.ht.discordbridge.discord.extensions.chatrelay.ChatRelayExtension
import dev.upcraft.ht.discordbridge.discord.extensions.commands.CommandProxyExtension
import dev.upcraft.ht.discordbridge.discord.extensions.server_status.ServerStatusExtension
import dev.upcraft.ht.discordbridge.discord.i18n.Translations
import dev.upcraft.ht.discordbridge.discord.util.*
import dev.upcraft.ht.discordbridge.model.player.PartialPlayerInfo
import dev.upcraft.ht.discordbridge.model.player.PlayerDisconnectReason
import dev.upcraft.ht.discordbridge.model.player.PlayerInfo
import dev.upcraft.ht.discordbridge.model.server.ServerStatus
import dev.upcraft.ht.discordbridge.service.BotService
import java.util.concurrent.CompletableFuture

@AutoService(BotService::class)
class AIOBotService : BotService {

    val botStartup: CompletableFuture<Void?> = CompletableFuture()

    override fun setStartupComplete() {
        botStartup.complete(null)
    }

    override fun botStartup(): CompletableFuture<Void?> = botStartup

    override fun onPlayerJoin(player: PlayerInfo) = runAsync<ServerStatusExtension> {
        val translatedMessage = Translations.Player.join.translateNamed(buildMap { addPlayerContext(player) })
        getJoinLeaveChannel()?.createMessage(translatedMessage)
        bot.editPresence { fromServerStatus() }
    }

    override fun onPlayerLeave(player: PartialPlayerInfo, reason: PlayerDisconnectReason) = runAsync<ServerStatusExtension> {
        val translationKey = when (reason.type) {
            PlayerDisconnectReason.Type.CLIENT_DISCONNECT -> Translations.Player.leave
            PlayerDisconnectReason.Type.SERVER_KICK -> if (reason.message == null) Translations.Player.kick else Translations.Player.kickReason
            else -> Translations.Player.lostConnection
        }
        val translatedMessage = translationKey.translateNamed(buildMap {
            addPlayerContext(player)
            this["reason"] = reason.message
        })

        getJoinLeaveChannel()?.createMessage(translatedMessage)

        bot.editPresence { fromServerStatus() }
    }

    override fun onServerStarted() = runAsync<ServerStatusExtension> {
        val translatedMessage = Translations.Server.started.translateNamed(buildMap {
            addServerContext()
        })
        getServerStatusChannel()?.createMessage(translatedMessage)
        bot.editPresence { fromServerStatus(ServerStatus.RUNNING) }
    }

    override fun onServerShutdown(): CompletableFuture<Void?> = runFuture<ServerStatusExtension> {
        val translatedMessage = Translations.Server.shutdown.translateNamed(buildMap {
            addServerContext()
        })
        getServerStatusChannel()?.createMessage(translatedMessage)
        bot.editPresence { fromServerStatus(ServerStatus.SHUTTING_DOWN) }
    }

    override fun onPlayerChat(player: PartialPlayerInfo, message: String) = runAsync<ChatRelayExtension> {
        val translatedMessage = Translations.Chat.message.translateNamed(buildMap {
            addMessageContext(message, player)
        })
        getChatRelayChannel()?.createMessage {
            content = translatedMessage
            // do not allow @everyone ping! never again!
            allowedMentions { }
        }
    }

    override fun sendCommandFeedback(message: String, originalMessageId: String?) = runAsync<CommandProxyExtension> {
        getChatRelayChannel()?.let { channel ->
            val originalMessage = originalMessageId?.let { channel.getMessageOrNull(Snowflake(it)) }

            message.chunked(maxTextMessageSize - 8).forEach {
                // TODO figure out a way translate this since we need to split the message beforehand
                val chunk = """
                    ```
                    $it
                    ```
                    """.trimIndent()

                originalMessage?.reply {
                    content = chunk
                    allowedMentions {

                    }
                } ?: run {
                    channel.createMessage(chunk)
                }
            }
        }
    }
}
