apply {
    plugin("io.quarkus")
}

dependencies {
    implementation(project(":domain:core")) // core implementation
    implementation(project(":admin-client")) // admin web-ui implementation
    implementation(project(":connector:http")) // connectors implementation
    databaseProviderImplementation()

    testImplementation(project(":common-test"))
    testImplementation(project(":domain:admin-api"))
    testImplementation(project(":domain:external-api"))
    testImplementation(project(":persistence:api"))

    testImplementation("io.quarkus:quarkus-resteasy-reactive")
}

/**
 * Resolves database provider dependency, depending on property passed for task
 */
fun DependencyHandler.databaseProviderImplementation(): Dependency? {
    // database provider names should be the same as modules in :persistence
    val supportedDatabaseProviders = setOf("h2", "postgresql")
    val databaseProviderProperty = "databaseProvider"

    val databaseProvider = project.properties[databaseProviderProperty] as? String ?: "h2" // h2 is a default value

    logger.info("Selected database provider: $databaseProvider")

    if (!supportedDatabaseProviders.contains(databaseProvider)) {
        throw GradleException("Invalid \"databaseProvider\" property \"$databaseProvider\" is set. Supported values: $supportedDatabaseProviders")
    }

    return implementation(project(":persistence:$databaseProvider"))
}
