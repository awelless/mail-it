pluginManagement {
    val jandexPluginVersion = "1.1.0" // https://plugins.gradle.org/plugin/org.kordamp.gradle.jandex
    val kotlinVersion = "1.9.22" // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib
    val ktlintVersion = "11.6.1" // https://mvnrepository.com/artifact/org.jlleitschuh.gradle/ktlint-gradle
    val springDependencyManagementVersion = "1.1.4" // // https://mvnrepository.com/artifact/io.spring.gradle/dependency-management-plugin
    val quarkusVersion = "3.7.4" // https://mvnrepository.com/artifact/io.quarkus.platform/quarkus-bom

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
    "admin-console",
    "api-key:api-key-api",
    "api-key:api-key-core",
    "api-key:api-key-quarkus",
    "api-key:api-key-spi-persistence",
    "api-key:api-key-spi-security",
    "api-key:api-key-test",
    "common-test",
    "connector:http",
    "core:admin-api",
    "core:connector-api",
    "core:exception",
    "core:model",
    "core:service",
    "core:spi",
    "distribution",
    "id-generator:id-generator-api",
    "id-generator:id-generator-core",
    "id-generator:id-generator-quarkus",
    "id-generator:id-generator-spi-locking",
    "id-generator:id-generator-test",
    "language-extensions",
    "persistence:common",
    "persistence:h2",
    "persistence:liquibase",
    "persistence:mysql",
    "persistence:postgresql",
    "persistence:tests",
    "template:template-api",
    "template:template-core",
    "template:template-quarkus",
    "template:template-spi-persistence",
    "template:template-test",
    "value-classes",
    "worker:worker-api",
    "worker:worker-core",
    "worker:worker-quarkus",
    "worker:worker-spi-persistence",
    "worker:worker-test",
)
