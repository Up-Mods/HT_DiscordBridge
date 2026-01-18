package dev.upcraft.ht.discordbridge.discord.util

import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.effectiveName
import dev.upcraft.ht.discordbridge.model.discord.DiscordUser
import java.util.*

internal val OptionalInt.orNull: Int?
    get() = if(this.isPresent) this.asInt else null

suspend fun MemberBehavior.asDiscordUser(): DiscordUser {
    val displayName = this.asMember().effectiveName
    return object: DiscordUser {
        override fun userId(): String = id.toString()

        override fun displayName(): String = displayName

        override fun mention(): String = mention
    }
}

suspend fun UserBehavior.asDiscordUser(): DiscordUser {
    val displayName = this.asUser().effectiveName
    return object: DiscordUser {
        override fun userId(): String = id.toString()

        override fun displayName(): String = displayName

        override fun mention(): String = mention
    }
}
