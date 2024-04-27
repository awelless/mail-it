dependencies {
    implementation(project(":core:exception"))
    implementation(project(":id-generator:id-generator-api"))
    implementation(project(":language-extensions"))
    implementation(project(":template:template-api"))
    implementation(project(":worker:worker-api"))
    implementation(project(":worker:worker-spi-mailing"))
    implementation(project(":worker:worker-spi-persistence"))

    testImplementation(project(":common-test"))
    testImplementation(project(":id-generator:id-generator-test"))
}
