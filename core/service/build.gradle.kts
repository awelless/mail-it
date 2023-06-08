dependencies {
    implementation(project(":core:admin-api"))
    implementation(project(":core:connector-api"))
    implementation(project(":core:exception"))
    implementation(project(":core:model"))
    implementation(project(":core:spi"))

    implementation("io.github.microutils:kotlin-logging-jvm")

    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-elytron-security")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-mailer")
    implementation("io.quarkus:quarkus-quartz")

    implementation("org.freemarker:freemarker")

    testImplementation(project(":common-test"))
}
