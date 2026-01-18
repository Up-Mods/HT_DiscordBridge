pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()

        maven("https://snapshots-repo.kordex.dev")
        maven("https://releases-repo.kordex.dev")

        maven("https://maven.hytale-modding.info/releases") {
            name = "HytaleModdingReleases"
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "DiscordBridge"

include("discord", "shared")
include("plugin-aio")
project(":plugin-aio").name = "DiscordBridge"
//include("plugin-standalone") // TODO implement
//project(":plugin-standalone").name = "DiscordBridge-Standalone"

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_SETTINGS
    repositories {
        mavenCentral()
        maven("https://snapshots-repo.kordex.dev")
        maven("https://releases-repo.kordex.dev")
        maven("https://mirror-repo.kordex.dev")
        maven("https://maven.hytale-modding.info/releases") {
            name = "HytaleModdingReleases"
        }
    }
}
