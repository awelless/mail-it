pluginManagement {
    val jandexPluginVersion = "0.13.2" // https://plugins.gradle.org/plugin/org.kordamp.gradle.jandex
    val kotlinVersion = "1.7.21" // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib
    val ktlintVersion = "11.1.0" // https://mvnrepository.com/artifact/org.jlleitschuh.gradle/ktlint-gradle
    val springDependencyManagementVersion = "1.1.0" // // https://mvnrepository.com/artifact/io.spring.gradle/dependency-management-plugin
    val quarkusVersion = "2.15.1.Final" // https://mvnrepository.com/artifact/io.quarkus.platform/quarkus-bom

    repositories {
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion

        id("io.quarkus") version quarkusVersion
        id("org.kordamp.gradle.jandex") version jandexPluginVersion

        id("io.spring.dependency-management") version springDependencyManagementVersion

        id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
    }
}

rootProject.name = "mail-it"

include(
    "admin-client",
    "common-test",
    "connector:http",
    "core:admin-api",
    "core:connector-api",
    "core:exception",
    "core:model",
    "core:service",
    "core:spi",
    "distribution",
    "persistence:common",
    "persistence:h2",
    "persistence:liquibase",
    "persistence:postgresql",
    "persistence:tests",
)
