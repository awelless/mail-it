dependencies {
    api(project(":core:model"))

    api("io.mockk:mockk")
    api("io.quarkus:quarkus-junit5")
    api("io.quarkus:quarkus-test-security")
    api("io.rest-assured:rest-assured")
    api("io.rest-assured:kotlin-extensions")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    api("org.jsoup:jsoup")

    implementation("io.quarkus:quarkus-arc")
}
