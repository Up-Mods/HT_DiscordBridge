package dev.upcraft.ht.discordbridge.discord.extensions

import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import dev.upcraft.ht.discordbridge.discord.service.AIOBotService
import dev.upcraft.ht.discordbridge.service.BotService
import dev.upcraft.ht.discordbridge.util.Services
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

class KordUtilExtension: Extension() {
    override val name = "kord_util"

    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                onKordIsReady(event.kord)
                Services.BOT.setStartupComplete()
            }
        }
    }
}

typealias Action = suspend Kord.() -> Unit

val queue = ArrayDeque<Action>()
@Volatile
private var hasSeenReady = false

internal suspend fun onKordIsReady(kord: Kord) {
    synchronized(::hasSeenReady) {
        hasSeenReady = true
    }
    if(!queue.isEmpty()) {
        with(kord) {
            var element: Action?
            synchronized(queue) {
                element = queue.removeFirstOrNull()
            }
            while(element != null) {
                element()
                synchronized(queue) {
                    element = queue.removeFirstOrNull()
                }
            }
        }
    }
}

suspend fun Kord.whenReady(action: Action) {
    if(!hasSeenReady) {
        synchronized(::hasSeenReady) {
            if(!hasSeenReady) {
                queue.add(action)
                return
            }
        }
    }

    launch { action() }.join()
}
