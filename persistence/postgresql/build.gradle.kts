apply {
    plugin("io.quarkus")
}

dependencies {
    implementation(project(":core:spi"))
    implementation(project(":persistence:common"))
    implementation(project(":persistence:liquibase"))

    implementation("io.github.microutils:kotlin-logging-jvm")

    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-kotlin")

    implementation("io.quarkus:quarkus-jdbc-postgresql")
    implementation("io.quarkus:quarkus-reactive-pg-client")

    implementation("io.smallrye.reactive:mutiny-kotlin")

    testImplementation(project(":persistence:tests"))
}

tasks.getByName("quarkusGenerateCodeTests") {
    dependsOn("jandex")
}
