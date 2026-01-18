package dev.upcraft.ht.discordbridge.discord

import dev.kordex.core.ExtensibleBot
import dev.kordex.modules.web.core.backend.utils.web
import dev.upcraft.ht.discordbridge.discord.extensions.KordUtilExtension
import dev.upcraft.ht.discordbridge.discord.extensions.chatrelay.ChatRelayExtension
import dev.upcraft.ht.discordbridge.discord.extensions.commands.CommandProxyExtension
import dev.upcraft.ht.discordbridge.discord.extensions.server_status.ServerStatusExtension
import dev.upcraft.ht.discordbridge.discord.util.StartupConfig
import dev.upcraft.ht.discordbridge.discord.util.fromServerStatus
import dev.upcraft.ht.discordbridge.model.server.ServerStatus
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture

object HtDiscordBot {
    suspend fun init(cfg: StartupConfig): ExtensibleBot {
        val bot = ExtensibleBot(cfg.token) {
            applicationCommands {
                defaultGuild(cfg.devGuild)
            }

            chatCommands {
                enabled = true
            }

            extensions {
                cfg.web?.let {
                    web {
                        hostname = it.frontendUrl
                        port = it.port
                    }
                }

                add { ServerStatusExtension(cfg) }
                add { ChatRelayExtension(cfg) }
                add { CommandProxyExtension(cfg) }
                add(::KordUtilExtension)
            }

            presence { fromServerStatus(ServerStatus.STARTING) }
        }

        _instance = bot

        return bot
    }

    private var _instance: ExtensibleBot? = null;

    val INSTANCE get() = _instance

    @JvmStatic
    @OptIn(DelicateCoroutinesApi::class)
    fun startAsync(cfg: StartupConfig): CompletableFuture<Void?> = GlobalScope.future {
        init(cfg).startAsync()
        return@future null
    }
}
