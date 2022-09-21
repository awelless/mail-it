pluginManagement {
    val jandexPluginVersion: String by settings
    val kotlinVersion: String by settings
    val quarkusVersion: String by settings
    val ktlintVersion: String by settings

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

        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion

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
