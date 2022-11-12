val freemarkerVersion: String by project

dependencies {
    implementation(project(":core:exception"))
    implementation(project(":core:admin-api"))
    implementation(project(":core:external-api"))
    implementation(project(":core:model"))
    implementation(project(":core:persistence-api"))

    implementation("io.quarkus:quarkus-mailer")
    implementation("io.quarkus:quarkus-quartz")

    implementation("io.smallrye.reactive:mutiny-kotlin")

    implementation("org.freemarker:freemarker:$freemarkerVersion")

    testImplementation(project(":common-test"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
