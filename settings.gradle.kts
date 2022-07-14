pluginManagement {
    val jandexPluginVersion = "0.12.0" // https://plugins.gradle.org/plugin/org.kordamp.gradle.jandex
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

        id("org.kordamp.gradle.jandex") version jandexPluginVersion
    }
}

rootProject.name = "mail-it"

include(
    "admin-client",
    "common-exception",
    "common-test",
    "connector:http",
    "distribution",
    "domain:admin-api",
    "domain:core",
    "domain:external-api",
    "domain:model",
    "persistence:api",
    "persistence:common",
    "persistence:h2",
    "persistence:liquibase",
    "persistence:postgresql",
    "persistence:tests",
)
