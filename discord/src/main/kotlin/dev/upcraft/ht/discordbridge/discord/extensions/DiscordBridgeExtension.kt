package dev.upcraft.ht.discordbridge.discord.extensions

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.MessageChannel
import dev.kordex.core.extensions.Extension
import dev.upcraft.ht.discordbridge.discord.util.StartupConfig
import dev.upcraft.ht.discordbridge.discord.util.logger

abstract class DiscordBridgeExtension(val cfg: StartupConfig) : Extension() {

    internal suspend fun getChatRelayChannel(): MessageChannel?
            = getBotChannel(cfg.channels.chatRelayChannel)

    internal suspend fun getServerStatusChannel(): MessageChannel?
            = getBotChannel(cfg.channels.serverStatusChannel)

    internal suspend fun getJoinLeaveChannel(): MessageChannel?
            = getBotChannel(cfg.channels.joinLeaveChannel)

    // TODO death messages (and other events?)
    internal suspend fun getDeathMessagesChannel(): MessageChannel?
            = getBotChannel(cfg.channels.deathMessagesChannel)

    internal suspend fun getBotChannel(channelId: Snowflake): MessageChannel? {
        return kord.getChannelOf<MessageChannel>(channelId)?: run {
            logger.error { "Unable to retrieve channel $channelId" }
            return null
        }
    }
}
