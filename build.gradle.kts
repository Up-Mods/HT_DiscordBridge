import java.text.SimpleDateFormat
import java.util.*

plugins {
    idea
    `java-library`
    `maven-publish`
    alias(libs.plugins.idea.ext)
}

val buildTime = Date()
val buildNumber: Int = providers.environmentVariable("BUILD_NUMBER").map { it.toInt() }.orElse(0).get()
val runningOnCI = providers.environmentVariable("CI").orNull.toBoolean()

group = "dev.upcraft.ht.discord-bridge"
version = SimpleDateFormat("YY.M.d").format(buildTime)

subprojects {
    apply {
        plugin("java-library")
        plugin("maven-publish")
    }
    group = rootProject.group
    version = rootProject.version

    java {
        withSourcesJar()
    }

    tasks.withType<Jar> {
        manifest {
            attributes["Specification-Title"] = rootProject.name
            attributes["Specification-Version"] = version
            attributes["Implementation-Title"] = "DiscordBot"
            attributes["Implementation-Version"] =
                providers.environmentVariable("COMMIT_SHA_SHORT")
                    .map { "${version}-${it}" }
                    .getOrElse(version.toString())
        }
    }
}

publishing {
    repositories {
        maven("https://maven.hytale-mods.dev/releases") {
            credentials {
                username = providers.environmentVariable("HM_RELEASES_MAVEN_USER").orNull
                password = providers.environmentVariable("HM_RELEASES_MAVEN_TOKEN").orNull
            }
        }
    }
}

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        isDownloadSources = !runningOnCI
        isDownloadJavadoc = !runningOnCI
    }
}

tasks.jar {
    enabled = false
}
