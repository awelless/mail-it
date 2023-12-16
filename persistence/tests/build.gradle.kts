dependencies {
    api(project(":common-test"))
    api(project(":core:spi"))
    api(project(":id-generator:id-generator-spi-locking"))

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
