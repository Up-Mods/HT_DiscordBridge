package dev.upcraft.ht.discordbridge.discord.util

import dev.kord.common.entity.Snowflake
import dev.kordex.core.utils.env
import dev.kordex.core.utils.envOf
import dev.kordex.core.utils.envOfOrNull
import dev.kordex.core.utils.envOrNull

const val placeholderSnowflake = "0000000000000000000"

data class StartupConfig(
    var token: String,
    var devGuild: Snowflake?,
    var web: WebConfig? = null,
    var roles: RolesConfig? = null,
    var channels: ChannelsConfig = ChannelsConfig.empty()
) {
    companion object {
        const val BOT_TOKEN_KEY = "DISCORD_BOT_TOKEN"
        const val BOT_TOKEN_DEFAULT_VALUE = "REPLACE_ME"

        @Throws(IllegalStateException::class)
        @JvmStatic
        fun fromEnv(): StartupConfig {
            return StartupConfig(
                token = env(BOT_TOKEN_KEY),
                devGuild = envOfOrNull<Snowflake>("DISCORD_DEVELOPMENT_GUILD"),
                web = null, // TODO web config
                roles = RolesConfig.fromEnv(),
                channels = ChannelsConfig.fromEnv(),
            )
        }

        @JvmStatic
        fun empty(): StartupConfig {
            return StartupConfig(
                token = BOT_TOKEN_DEFAULT_VALUE,
                devGuild = null,
                web = null,
                roles = RolesConfig.empty()
            )
        }
    }

    var devGuildString
        get() = devGuild?.toString()
        set(value){ devGuild = value?.let { Snowflake(it) } }
}

data class WebConfig(
    var frontendUrl: String?,
    var port: Int,
)

data class RolesConfig(
    var serverOperators: List<Snowflake>
) {
    companion object {
        @JvmStatic
        fun empty(): RolesConfig {
            return RolesConfig(
                serverOperators = listOf(Snowflake(placeholderSnowflake))
            )
        }

        fun fromEnv(): RolesConfig {
            return RolesConfig(
                serverOperators = envOrNull("SERVER_OPERATORS")?.split(Regex(",\\s*"))?.filter { it.isNotBlank() }
                    ?.map {
                        Snowflake(
                            it
                        )
                    } ?: listOf()
            )
        }
    }

    var serverOperatorsArray
        get() = serverOperators.map { it.toString() }.toTypedArray()
        set(value) { serverOperators = value.map { Snowflake(it) } }
}

data class ChannelsConfig(
    var defaultChannel: Snowflake = Snowflake(placeholderSnowflake)
) {
    companion object {
        @JvmStatic
        fun empty(): ChannelsConfig {
            return ChannelsConfig ()
        }

        fun fromEnv(): ChannelsConfig {
            // TODO remove eventually
            val legacyDefaultChannel = envOfOrNull<Snowflake>("DISCORD_NOTIFICATION_CHANNEL_ID")

            if(legacyDefaultChannel != null) {
                serviceLogger.warn { "DISCORD_NOTIFICATION_CHANNEL_ID env var is deprecated and will be removed in a future update. Please migrate to DISCORD_DEFAULT_CHANNEL_ID !!!" }
            }

            return ChannelsConfig(
                defaultChannel = envOfOrNull<Snowflake>("DISCORD_DEFAULT_CHANNEL_ID")
                    ?: legacyDefaultChannel
                    ?: envOf<Snowflake>("DISCORD_DEFAULT_CHANNEL_ID") // this is here so it throws the correct error.
            )
        }
    }

    var defaultChannelString
        get() = defaultChannel.toString()
        set(value) { defaultChannel = Snowflake(value) }

    //TODO implement configurable channels
    val chatRelayChannel get() = defaultChannel
    val joinLeaveChannel get() = defaultChannel
    val deathMessagesChannel get() = defaultChannel
    val serverStatusChannel get() = defaultChannel
}
