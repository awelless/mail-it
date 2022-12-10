dependencies {
    api(project(":common-test"))
    api(project(":core:spi"))
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }
    }
}
