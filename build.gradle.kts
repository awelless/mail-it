import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.spotless.LineEnding
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")

    id("io.quarkus") apply false

    id("com.diffplug.spotless")
}

allprojects {

}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.allopen")
        plugin("io.quarkus")
        plugin("com.diffplug.spotless")
    }

    group = "it.mail"
    version = "0.1.0"

    repositories {
        mavenCentral()
        mavenLocal()
    }

    val dbUtilsVersion = "1.7" // https://mvnrepository.com/artifact/commons-dbutils/commons-dbutils
    val freemarkerVersion = "2.3.31" // https://mvnrepository.com/artifact/org.freemarker/freemarker
    val kotlinLoggingVersion = "2.1.23" // https://mvnrepository.com/artifact/io.github.microutils/kotlin-logging
    val kryoVersion = "5.3.0" // https://mvnrepository.com/artifact/com.esotericsoftware/kryo
    val mockkVersion = "1.12.4" // https://mvnrepository.com/artifact/io.mockk/mockk
    val quarkusVersion: String by project // https://mvnrepository.com/artifact/io.quarkus.platform/quarkus-bom
    val restAssuredKotlinExtensionsVersion = "5.1.1" // https://mvnrepository.com/artifact/io.rest-assured/kotlin-extensions

    dependencies {
        implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))

        implementation("com.esotericsoftware:kryo:$kryoVersion")

        implementation("commons-dbutils:commons-dbutils:$dbUtilsVersion")
        implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

        implementation("io.quarkus:quarkus-arc")
        implementation("io.quarkus:quarkus-agroal")
        implementation("io.quarkus:quarkus-config-yaml")
        implementation("io.quarkus:quarkus-jdbc-h2")
        implementation("io.quarkus:quarkus-jdbc-postgresql")
        implementation("io.quarkus:quarkus-kotlin")
        implementation("io.quarkus:quarkus-liquibase")
        implementation("io.quarkus:quarkus-mailer")
        implementation("io.quarkus:quarkus-quartz")
        implementation("io.quarkus:quarkus-reactive-pg-client")
        implementation("io.quarkus:quarkus-resteasy-reactive")
        implementation("io.quarkus:quarkus-resteasy-reactive-jackson")

        implementation("io.smallrye.reactive:mutiny-kotlin")

        implementation("org.freemarker:freemarker:$freemarkerVersion")

        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

        testImplementation("io.mockk:mockk:$mockkVersion")
        testImplementation("io.quarkus:quarkus-junit5")
        testImplementation("io.rest-assured:rest-assured")
        testImplementation("io.rest-assured:kotlin-extensions:$restAssuredKotlinExtensionsVersion")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
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
