import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")

    id("io.quarkus")

    id("com.diffplug.spotless")
}

repositories {
    mavenCentral()
    mavenLocal()
}

var dbUtilsVersion = "1.7"
val kotlinLoggingVersion = "2.1.21"
val mockkVersion = "1.12.3"
val quarkusVersion: String by project
val restAssuredKotlinExtensionsVersion = "5.0.1"

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))

    implementation("commons-dbutils:commons-dbutils:$dbUtilsVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-agroal")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-jdbc-h2")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-mailer")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")

    implementation("io.smallrye.reactive:mutiny-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.rest-assured:kotlin-extensions:$restAssuredKotlinExtensionsVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}

group = "it.mail"
version = "0.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
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

task("copyGitHooks", Copy::class) {
    from("git")
    into(".git/hooks")
}
