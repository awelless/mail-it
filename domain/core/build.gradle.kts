val freemarkerVersion = "2.3.31" // https://mvnrepository.com/artifact/org.freemarker/freemarker

dependencies {
    implementation(project(":common-exception"))
    implementation(project(":domain:admin-api"))
    implementation(project(":domain:external-api"))
    implementation(project(":domain:model"))
    implementation(project(":persistence:api"))

    implementation("io.quarkus:quarkus-mailer")
    implementation("io.quarkus:quarkus-quartz")

    implementation("org.freemarker:freemarker:$freemarkerVersion")

    testImplementation(project(":common-test"))
}
