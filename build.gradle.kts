import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")

    id("io.quarkus") apply false

    id("com.diffplug.spotless")

    id("org.kordamp.gradle.jandex") apply false
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.allopen")
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

    val kotlinLoggingVersion = "2.1.23" // https://mvnrepository.com/artifact/io.github.microutils/kotlin-logging
    val quarkusVersion: String by project // https://mvnrepository.com/artifact/io.quarkus.platform/quarkus-bom

    dependencies {
        implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))

        implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

        implementation("io.quarkus:quarkus-arc")
        implementation("io.quarkus:quarkus-config-yaml")
        implementation("io.quarkus:quarkus-kotlin")
        implementation("io.quarkus:quarkus-resteasy-reactive")
        implementation("io.quarkus:quarkus-resteasy-reactive-jackson")

        implementation("io.smallrye.reactive:mutiny-kotlin")

        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    allOpen {
        annotation("javax.ws.rs.Path")
        annotation("javax.inject.Singleton")
        annotation("io.quarkus.test.junit.QuarkusTest")
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
