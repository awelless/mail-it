dependencies {
    implementation(project(":id-generator:id-generator-api"))
    implementation(project(":id-generator:id-generator-spi-locking"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    testImplementation(project(":common-test"))
}
