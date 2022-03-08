plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    kotlin("plugin.jpa")

    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusVersion: String by project
val mockkVersion = "1.12.3"

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))

    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-flyway")
    implementation("io.quarkus:quarkus-hibernate-orm-panache-kotlin")
    implementation("io.quarkus:quarkus-jdbc-h2")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-mailer")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")

    implementation("io.github.microutils:kotlin-logging-jvm:2.1.20")


    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "it.mail"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")

    annotations("javax.persistence.Entity", "javax.persistence.MappedSuperclass", "javax.persistence.Embedabble")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    kotlinOptions.javaParameters = true
}
