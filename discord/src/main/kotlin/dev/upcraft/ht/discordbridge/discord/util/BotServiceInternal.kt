package dev.upcraft.ht.discordbridge.discord.util

import dev.upcraft.ht.discordbridge.service.BotService

internal interface BotServiceInternal : BotService {

    fun setStartupComplete()
}
