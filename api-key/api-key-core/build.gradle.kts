dependencies {
    implementation(project(":api-key:api-key-api"))
    implementation(project(":api-key:api-key-spi-persistence"))
    implementation(project(":api-key:api-key-spi-security"))
    implementation(project(":core:exception"))
    implementation(project(":language-extensions"))

    testImplementation(project(":common-test"))
}
