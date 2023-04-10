import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.allopen") apply false

    id("io.quarkus") apply false
    id("org.kordamp.gradle.jandex") apply false

    id("org.jlleitschuh.gradle.ktlint") apply false

    id("io.spring.dependency-management") apply true
}

repositories {
    mavenCentral()
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("org.kordamp.gradle.jandex")
        plugin("io.spring.dependency-management")
    }

    repositories {
        mavenCentral()
    }

    group = "io.mailit"
    version = "0.2.0"

    val dbUtilsVersion = "1.7" // https://mvnrepository.com/artifact/commons-dbutils/commons-dbutils
    val freemarkerVersion = "2.3.32" // https://mvnrepository.com/artifact/org.freemarker/freemarker
    val jsoupVersion = "1.15.4" // https://mvnrepository.com/artifact/org.jsoup/jsoup
    val kotlinLoggingVersion = "3.0.5" // https://mvnrepository.com/artifact/io.github.microutils/kotlin-logging
    val kryoVersion = "5.4.0" // https://mvnrepository.com/artifact/com.esotericsoftware/kryo
    val quarkusVersion = "2.16.6.Final" // https://mvnrepository.com/artifact/io.quarkus.platform/quarkus-bom
    val restAssuredKotlinExtensionsVersion = "5.3.0" // https://mvnrepository.com/artifact/io.rest-assured/kotlin-extensions
    val svmVersion = "22.3.1" // https://mvnrepository.com/artifact/org.graalvm.nativeimage/svm

    val mockkVersion = "1.13.4" // https://mvnrepository.com/artifact/io.mockk/mockk

    dependencyManagement {
        dependencies {
            // persistence
            dependency("com.esotericsoftware:kryo:$kryoVersion")
            dependency("commons-dbutils:commons-dbutils:$dbUtilsVersion")

            // logging
            dependency("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

            // templates
            dependency("org.freemarker:freemarker:$freemarkerVersion")

            // native
            dependency("org.graalvm.nativeimage:svm:$svmVersion")

            // tests
            dependency("io.mockk:mockk:$mockkVersion")
            dependency("io.rest-assured:kotlin-extensions:$restAssuredKotlinExtensionsVersion")
            dependency("org.jsoup:jsoup:$jsoupVersion")
        }
    }

    dependencies {
        implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))

        implementation("org.jetbrains.kotlin:kotlin-stdlib")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
        kotlinOptions.javaParameters = true
    }

    tasks.getByName("compileTestKotlin") {
        dependsOn("jandex")
    }

    kotlin {
        sourceSets {
            test {
                languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

task<Copy>("setUpGitHooks") {
    from("git")
    into(".git/hooks")
}
