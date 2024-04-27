dependencies {
    implementation(project(":core:exception"))
    implementation(project(":id-generator:id-generator-api"))
    implementation(project(":template:template-api"))
    implementation(project(":worker:worker-api"))
    implementation(project(":worker:worker-core"))
    implementation(project(":worker:worker-spi-mailing"))
    implementation(project(":worker:worker-spi-persistence"))

    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-mailer")
}
