dependencies {
    api(project(":api-key:api-key-spi-persistence"))
    api(project(":common-test"))
    api(project(":core:exception"))
    api(project(":core:spi"))
    api(project(":id-generator:id-generator-spi-locking"))
    api(project(":template:template-spi-persistence"))
    api(project(":worker:worker-spi-persistence"))

    implementation(project(":persistence:common"))
    implementation("io.quarkus:quarkus-liquibase")
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }
    }
}
