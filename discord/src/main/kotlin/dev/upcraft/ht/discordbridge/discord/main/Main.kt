package dev.upcraft.ht.discordbridge.discord.main

import dev.upcraft.ht.discordbridge.discord.HtDiscordBot
import dev.upcraft.ht.discordbridge.discord.util.StartupConfig
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security

suspend fun main() {
    Security.addProvider(BouncyCastleProvider())

    println("Launching in ${System.getProperty("user.dir")}")

    val cfg = StartupConfig.fromEnv()
    HtDiscordBot.init(cfg).start()
}
