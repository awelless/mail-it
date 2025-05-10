dependencies {
    implementation(project(":api-key:api-key-api"))
    implementation(project(":api-key:api-key-spi-persistence"))
    implementation(project(":api-key:api-key-spi-security"))
    implementation(project(":language-extensions"))
    implementation(project(":value-classes"))

    testImplementation(project(":common-test"))
}
