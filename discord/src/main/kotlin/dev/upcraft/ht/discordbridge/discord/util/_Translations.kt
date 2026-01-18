package dev.upcraft.ht.discordbridge.discord.util

import dev.upcraft.ht.discordbridge.discord.i18n.Translations
import dev.upcraft.ht.discordbridge.model.player.PartialPlayerInfo
import java.util.*

fun MutableMap<String, Any?>.addPlayerContext(player: PartialPlayerInfo, name: String = "player") {
    addPlayerContext(player.displayName, player.uuid, name)
}

fun MutableMap<String, Any?>.addPlayerContext(username: String?, uuid: UUID?, name: String = "player") {
    username?.let { this["${name}.username"] = it }
    uuid?.let { this["${name}.uuid"] = it }
    addServerContext()
}

fun MutableMap<String, Any?>.addMessageContext(message: String?, sender: PartialPlayerInfo? = null) {
    this["message.content"] = message
    sender?.let { addPlayerContext(it) }
}

fun MutableMap<String, Any?>.addStatusContext() {
    addServerContext()
}

fun MutableMap<String, Any?>.addServerContext() {
    this["server.onlinePlayerCount"] = Hytale.maxPlayers.orNull?.let {
        Translations.formatPlayerCount.translateNamed(
            "player_count" to Hytale.onlinePlayers,
            "max_player_count" to it
        )
    } ?: Translations.formatPlayerCountUnlimited.translateNamed(
        "player_count" to Hytale.onlinePlayers
    )
}
