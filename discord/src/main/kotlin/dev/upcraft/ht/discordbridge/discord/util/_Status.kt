package dev.upcraft.ht.discordbridge.discord.util

import dev.kord.common.entity.PresenceStatus
import dev.kord.gateway.builder.PresenceBuilder
import dev.upcraft.ht.discordbridge.discord.i18n.Translations
import dev.upcraft.ht.discordbridge.model.server.ServerStatus

fun PresenceBuilder.fromServerStatus(serverStatus: ServerStatus = Hytale.serverStatus) {
    val (newStatus, newState) = when (serverStatus) {
        ServerStatus.RUNNING -> PresenceStatus.Online to Translations.BotStatus.online.translateNamed(buildMap { addStatusContext() })
        ServerStatus.STARTING -> PresenceStatus.Idle to Translations.BotStatus.starting.translateNamed(buildMap { addStatusContext() })
        ServerStatus.SHUTTING_DOWN -> PresenceStatus.Offline to null
        else -> PresenceStatus.Idle to null
    }
    status = newStatus
    state = newState
}
