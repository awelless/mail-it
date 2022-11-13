import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.allopen") apply false

    id("io.quarkus") apply false

    id("org.jlleitschuh.gradle.ktlint") apply false

    id("org.kordamp.gradle.jandex") apply false
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("org.kordamp.gradle.jandex")
    }

    repositories {
        mavenCentral()
        mavenLocal()
    }

    group = "it.mail"
    version = "0.1.0"

    val kotlinLoggingVersion: String by project
    val quarkusVersion: String by project

    dependencies {
        implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))

        implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

        implementation("io.quarkus:quarkus-arc")
        implementation("io.quarkus:quarkus-config-yaml")
        implementation("io.quarkus:quarkus-kotlin")

        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
        kotlinOptions.javaParameters = true
    }

    kotlin {
        sourceSets {
            test {
                languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }
    }
}

task<Copy>("setUpGitHooks") {
    from("git")
    into(".git/hooks")
}
