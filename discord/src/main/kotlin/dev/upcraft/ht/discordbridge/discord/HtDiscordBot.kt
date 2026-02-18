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
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

object HtDiscordBot {
    suspend fun init(cfg: StartupConfig, pluginSearchPath: Path? = null): ExtensibleBot {
        val bot = ExtensibleBot(cfg.token) {
            plugins {
                pluginSearchPath?.let {
                    pluginPaths.clear()
                    pluginPaths.add(it)
                }
            }

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

            plugins {

            }

            kord {
                httpClient = HttpClient(Java) {
                    install(ContentNegotiation) {
                        json(Json {
                            encodeDefaults = false
                            allowStructuredMapKeys = true
                            ignoreUnknownKeys = true
                            isLenient = true
                        })
                    }

                    install(HttpTimeout) {
                        requestTimeoutMillis = 1000000
                        socketTimeoutMillis = 1000000
                    }

                    install(WebSockets)
                }
            }
        }

        _instance = bot

        return bot
    }

    private var _instance: ExtensibleBot? = null;

    val INSTANCE get() = _instance

    @JvmStatic
    @OptIn(DelicateCoroutinesApi::class)
    fun startAsync(cfg: StartupConfig, pluginSearchPath: Path?): CompletableFuture<Void?> = GlobalScope.future {
        init(cfg, pluginSearchPath).startAsync().join()
        return@future null
    }
}
