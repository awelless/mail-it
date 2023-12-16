dependencies {
    implementation(project(":id-generator:id-generator-api"))
    implementation(project(":id-generator:id-generator-core"))
    implementation(project(":id-generator:id-generator-spi-locking"))

    implementation("io.quarkus:quarkus-kotlin")
}
