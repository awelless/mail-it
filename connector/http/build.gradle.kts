plugins {
    id("org.jetbrains.kotlin.plugin.allopen")
}

dependencies {
    implementation(project(":core:connector-api"))

    implementation("io.github.microutils:kotlin-logging-jvm")

    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
}
