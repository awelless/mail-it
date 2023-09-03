dependencies {
    api(project(":common-test"))
    api(project(":core:spi"))

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
