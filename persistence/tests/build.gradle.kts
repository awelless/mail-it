dependencies {
    api(project(":common-test"))
    api(project(":core:persistence-api"))
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }
    }
}
