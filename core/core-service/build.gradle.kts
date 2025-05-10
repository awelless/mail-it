dependencies {
    // Own api modules.
    implementation(project(":core:core-api-admin"))
    implementation(project(":core:core-api-connector"))
    implementation(project(":core:core-api-scheduler"))

    // Own spi.
    implementation(project(":core:core-spi-persistence"))

    // Dependencies on other modules.
    implementation(project(":id-generator:id-generator-api"))
    implementation(project(":template:template-api"))
    implementation(project(":value-classes"))

    // Own submodules.
    implementation(project(":core:core-model"))

    // External dependencies.
    implementation("io.smallrye.reactive:mutiny-kotlin")

    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-elytron-security")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-mailer")

    implementation("org.freemarker:freemarker")

    testImplementation(project(":common-test"))
    testImplementation(project(":id-generator:id-generator-test"))
    testImplementation(project(":template:template-test"))
}
