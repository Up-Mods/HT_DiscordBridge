plugins {
    `java-library`
}

val javaVersion = 21 // FIXME kotlin does not support java 25 yet

dependencies {
    compileOnly(libs.jspecify)
    compileOnly(libs.jetbrains.annotations)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}
