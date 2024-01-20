dependencies {
    implementation(project(":api-key:api-key-api"))
    implementation(project(":api-key:api-key-core"))
    implementation(project(":api-key:api-key-spi-persistence"))
    implementation(project(":api-key:api-key-spi-security"))

    implementation("io.quarkus:quarkus-elytron-security")
    implementation("io.quarkus:quarkus-kotlin")
}
