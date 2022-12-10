val freemarkerVersion: String by project

dependencies {
    implementation(project(":core:admin-api"))
    implementation(project(":core:connector-api"))
    implementation(project(":core:exception"))
    implementation(project(":core:model"))
    implementation(project(":core:spi"))

    implementation("io.quarkus:quarkus-mailer")
    implementation("io.quarkus:quarkus-quartz")

    implementation("io.smallrye.reactive:mutiny-kotlin")

    implementation("org.freemarker:freemarker:$freemarkerVersion")

    testImplementation(project(":common-test"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
