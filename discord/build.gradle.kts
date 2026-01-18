
import dev.kordex.gradle.plugins.kordex.DataCollection
import dev.kordex.i18n.files.YamlFormat
import dev.kordex.i18n.messages.formats.ICUFormatV2

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kordex.plugin)
    alias(libs.plugins.kordex.plugin.i18n)
}

val javaVersion = 21 // FIXME kotlin does not support java 25 yet

dependencies {
    implementation(project(":shared"))

    compileOnly(libs.autoservice.annotations)
    ksp(libs.autoservice.ksp)

    // only used in standalone mode
    compileOnly(libs.bouncycastle)
}

kordEx {
    // https://github.com/gradle/gradle/issues/31383
    kordExVersion = libs.versions.kordex.asProvider()

    bot {
        dataCollection(DataCollection.None) // TODO change once embargo is lifted
        version = project.version.toString()

        mainClass = "dev.upcraft.ht.discordbridge.bot.main.MainKt"
    }

    jvmTarget = javaVersion

    addRepositories = false
    addThirdPartyRepositories = false

    module("web-backend")
}

i18n {
    bundle("htbridge.strings", "dev.upcraft.ht.discordbridge.discord.i18n")

    defaults {
        fileFormat = YamlFormat
        messageFormat = ICUFormatV2.identifier
    }
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

tasks.assemble {
    dependsOn(tasks["installDist"])
}
