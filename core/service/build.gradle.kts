dependencies {
    implementation(project(":core:admin-api"))
    implementation(project(":core:connector-api"))
    implementation(project(":core:scheduler-api"))

    implementation(project(":core:exception"))
    implementation(project(":core:model"))
    implementation(project(":core:spi"))
    implementation(project(":id-generator:id-generator-api"))
    implementation(project(":template:template-api"))

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
