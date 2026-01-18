package dev.upcraft.ht.discordbridge.discord.util

import dev.kord.gateway.builder.PresenceBuilder
import dev.kordex.core.ExtensibleBot
import dev.upcraft.ht.discordbridge.discord.extensions.whenReady

suspend fun ExtensibleBot.editPresence(builder: PresenceBuilder.() -> Unit) {
    kordRef.whenReady { editPresence { builder() } }
}
