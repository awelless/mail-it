import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.allopen") apply false

    id("io.quarkus") apply false

    id("com.diffplug.spotless")

    id("org.kordamp.gradle.jandex") apply false
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("io.quarkus")
        plugin("com.diffplug.spotless")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
        kotlinOptions.javaParameters = true
    }

    configure<SpotlessExtension> {
        kotlin {
            lineEndings = LineEnding.UNIX
            encoding("UTF-8")
            endWithNewline()
            trimTrailingWhitespace()

            ktlint()
        }

        kotlinGradle {
            lineEndings = LineEnding.UNIX
            encoding("UTF-8")
            endWithNewline()
            trimTrailingWhitespace()

            ktlint()
        }

        format("misc") {
            target(".gitignore", "**/*.yml", "**/*.xml")
            targetExclude(".gradle/**", ".idea/**", "build/**")

            lineEndings = LineEnding.UNIX
            encoding("UTF-8")
            endWithNewline()
            trimTrailingWhitespace()
        }
    }
}

task("copyGitHooks", Copy::class) {
    from("git")
    into(".git/hooks")
}
