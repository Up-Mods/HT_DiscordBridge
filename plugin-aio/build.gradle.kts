import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.hytale)
    alias(libs.plugins.shadow)
}

val javaVersion = 25
val buildNumber: Int = providers.environmentVariable("BUILD_NUMBER").map { it.toInt() }.orElse(0).get()

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.jspecify)

    implementation(libs.aspect)
//    implementation(libs.kotlin.libraries)

    shadow(project(":discord"))
    implementation(project(":discord"))
    shadow(project(":shared"))
    implementation(project(":shared"))
    shadow(libs.kx.coroutines.core.jvm)
    implementation(libs.kx.coroutines.core.jvm)

    shadow(libs.slf4j.jul)
    runtimeOnly(libs.slf4j.jul)

    compileOnly(libs.autoservice.annotations)
    annotationProcessor(libs.autoservice)
}

hytale {
    runDir = rootProject.file("run").path
}

tasks.named<ProcessResources>("processResources") {
    var replaceProperties = mapOf(
        "plugin_group" to findProperty("plugin_group"),
        "plugin_name" to project.name,
        "plugin_version" to "${project.version}+${buildNumber}",
        "server_version" to findProperty("server_version"),

        "plugin_description" to findProperty("plugin_description"),
        "plugin_website" to findProperty("plugin_website"),

        "plugin_main_entrypoint" to "dev.upcraft.ht.discordbridge.plugin.DiscordBridgeAIO",

        "aspect_version" to libs.versions.aspect.get()
    )

    filesMatching("manifest.json") {
        expand(replaceProperties)
    }

    inputs.properties(replaceProperties)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

tasks.jar {
    archiveClassifier = "slim"
}

val shadowJar = tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier = null
    configurations = project.configurations.shadow.map { listOf(it) }
}

tasks.assemble.configure {
    dependsOn(shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("shadow") {
            from(components["shadow"])
        }
    }
}
