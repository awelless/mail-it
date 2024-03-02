dependencies {
    implementation(project(":id-generator:id-generator-api"))
    implementation(project(":worker:worker-api"))
    implementation(project(":worker:worker-core"))
    implementation(project(":worker:worker-spi-persistence"))

    implementation("io.quarkus:quarkus-kotlin")
}
