dependencies {
    implementation(project(":core:exception"))
    implementation(project(":template:template-api"))
    implementation(project(":template:template-spi-persistence"))

    implementation("org.freemarker:freemarker")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

    testImplementation(project(":common-test"))
}
