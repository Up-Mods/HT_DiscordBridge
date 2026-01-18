package dev.upcraft.ht.discordbridge.discord.extensions.commands

import dev.kord.core.behavior.reply
import dev.kordex.core.checks.isNotBot
import dev.kordex.core.checks.isNotInThread
import dev.kordex.core.checks.memberFor
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.commands.converters.impl.stringList
import dev.kordex.core.extensions.chatCommand
import dev.upcraft.ht.discordbridge.commands.WhitelistCommand
import dev.upcraft.ht.discordbridge.discord.extensions.DiscordBridgeExtension
import dev.upcraft.ht.discordbridge.discord.i18n.Translations
import dev.upcraft.ht.discordbridge.discord.util.Hytale
import dev.upcraft.ht.discordbridge.discord.util.StartupConfig
import dev.upcraft.ht.discordbridge.discord.util.addPlayerContext
import dev.upcraft.ht.discordbridge.discord.util.asDiscordUser
import kotlinx.coroutines.future.await

class CommandProxyExtension(cfg: StartupConfig) : DiscordBridgeExtension(cfg) {
    override val name = "command_proxy"

    val operatorRoles = cfg.roles?.serverOperators ?: listOf()

    override suspend fun setup() {
        chatCommand(::ConsoleCommandArgs) {
            name = Translations.Commands.Console.name
            description = Translations.Commands.Console.description

            check {
                isNotBot()
                isNotInThread()
                failIf {
                    event.message.channelId != cfg.channels.chatRelayChannel
                }
                failIfNot {
                    memberFor(event)?.asMember()?.let {
                        operatorRoles.contains(it.id) || operatorRoles.any { roleId -> it.roleIds.contains(roleId) }
                    } ?: false
                }
            }

            action {
                val messageId = event.message.id
                val source = message.getAuthorAsMember()
                var command = arguments.command.joinToString(" ")

                // need to strip leading slashes
                if (command.startsWith('/')) {
                    command = command.substring(1)
                }

                Hytale.executeServerCommand(source.asDiscordUser(), command, messageId.toString()).await()
            }
        }

        chatCommand(::WhitelistCommandArgs) {
            name = Translations.Commands.Whitelist.name
            description = Translations.Commands.Whitelist.description

            check {
                isNotBot()
                isNotInThread()
                failIf {
                    event.message.channelId != cfg.channels.chatRelayChannel
                }
                failIfNot {
                    memberFor(event)?.asMember()?.let {
                        operatorRoles.contains(it.id) || operatorRoles.any { roleId -> it.roleIds.contains(roleId) }
                    } ?: false
                }
            }

            action {
                val messageId = event.message.id
                val source = message.getAuthorAsMember()
                val playerUsername = arguments.username

                val result = Hytale.whitelistPlayer(source.asDiscordUser(), playerUsername, messageId.toString()).await()
                val message = when(result.type) {
                    WhitelistCommand.ResultType.SUCCESS -> Translations.Commands.Whitelist.Result.success
                    WhitelistCommand.ResultType.ALREADY_PENDING -> Translations.Commands.Whitelist.Result.alreadyPending
                    WhitelistCommand.ResultType.ALREADY_WHITELISTED -> Translations.Commands.Whitelist.Result.alreadyAdded
                }
                event.message.reply {
                    content = message.translateNamed(buildMap {
                        result.playerInfo?.let { addPlayerContext(it) }
                            ?: run { addPlayerContext(playerUsername, null) }
                    })
                }
            }
        }
    }

    class ConsoleCommandArgs : Arguments() {
        val command by stringList {
            name = Translations.Commands.Console.Args.Command.name
            description = Translations.Commands.Console.Args.Command.description
        }
    }

    class WhitelistCommandArgs : Arguments() {
        val username by string {
            name = Translations.Commands.Whitelist.Args.Username.name
            description = Translations.Commands.Whitelist.Args.Username.description
        }
    }
}
