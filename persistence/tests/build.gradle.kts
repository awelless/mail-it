dependencies {
    api(project(":common-test"))

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
