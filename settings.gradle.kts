pluginManagement {
    val kotlinVersion: String by settings
    val quarkusVersion: String by settings
    val spotlessVersion = "6.3.0"

    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion

        id("io.quarkus") version quarkusVersion

        id("com.diffplug.spotless") version spotlessVersion
    }
}

rootProject.name = "mail-it"

include(
    "core",
)
