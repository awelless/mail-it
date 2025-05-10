apply {
    plugin("io.quarkus")
}

dependencies {
    implementation(project(":core:core-spi-mailer"))

    implementation("io.smallrye.reactive:mutiny-kotlin")

    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-mailer")
}
