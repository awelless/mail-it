plugins {
    id("org.jetbrains.kotlin.plugin.allopen")
}

dependencies {
    implementation(project(":api-key:api-key-api"))
    implementation(project(":core:exception"))
    implementation(project(":worker:worker-api"))

    implementation("io.github.microutils:kotlin-logging-jvm")

    implementation("io.vertx:vertx-lang-kotlin-coroutines")

    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
}
