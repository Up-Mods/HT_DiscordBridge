package dev.upcraft.ht.discordbridge.discord.util

import dev.kordex.core.extensions.Extension
import dev.upcraft.ht.discordbridge.discord.HtDiscordBot
import dev.upcraft.ht.discordbridge.service.HytaleService
import dev.upcraft.ht.discordbridge.util.Services
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture

val Hytale: HytaleService by lazy { Services.load(HytaleService::class.java) }
val serviceLogger: KLogger by lazy { KotlinLogging.logger {} }

val <T: Extension> T.logger: KLogger by lazy { KotlinLogging.logger {} }

@OptIn(DelicateCoroutinesApi::class)
inline fun <reified T: Extension> runFuture(crossinline action: suspend T.() -> Unit): CompletableFuture<Void?> {
    return HtDiscordBot.INSTANCE?.let {
        GlobalScope.future(it.kordRef.coroutineContext) {
            it.findExtension<T>()?.apply {
                action()
            } ?: run {
                serviceLogger.error { "Unable to find extension ${T::class}!" }
            }

            return@future null
        }
    } ?: run {
        serviceLogger.warn(NullPointerException()) { "Unable to retrieve bot instance!" }
        return CompletableFuture.completedFuture(null)
    }
}

inline fun <reified T: Extension> runAsync(crossinline action: suspend T.() -> Unit) {
    HtDiscordBot.INSTANCE?.let {
        it.kordRef.launch {
            it.findExtension<T>()?.apply {
                action()
            } ?: run {
                serviceLogger.error(NoSuchElementException("${T::class}")) { "Unable to find extension ${T::class}!" }
            }
        }
    } ?: run {
        serviceLogger.warn(NullPointerException()) { "Unable to retrieve bot instance!" }
    }
}
