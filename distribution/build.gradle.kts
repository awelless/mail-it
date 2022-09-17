apply {
    plugin("io.quarkus")
}

dependencies {
    implementation(project(":domain:core")) // core implementation
    implementation(project(":admin-client")) // admin web-ui implementation
    databaseProviderImplementation()
    connectorsImplementation()

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

    if (!supportedDatabaseProviders.contains(databaseProvider)) {
        throw GradleException("Invalid \"databaseProvider\" property \"$databaseProvider\" is set. Supported values: $supportedDatabaseProviders")
    }

    logger.info("Selected database provider: $databaseProvider")

    return implementation(project(":persistence:$databaseProvider"))
}

fun DependencyHandler.connectorsImplementation() {
    // connector names should be the same as modules in :connector
    val supportedConnectors = setOf("http")
    val connectorsProperty = "connectors"

    val connectorsValue = project.properties[connectorsProperty] as? String ?: "http" // http is a default value

    val connectors = connectorsValue.split(',')
        .map { it.trim() }

    if (connectors.isEmpty()) {
        throw GradleException("No connectors are specified")
    }

    connectors.forEach {
        if (!supportedConnectors.contains(it)) {
            throw GradleException("Invalid connector \"$it\" is set. Supported connectors: $supportedConnectors")
        }
    }

    logger.info("Selected connectors: $connectors")

    connectors.forEach { implementation(project(":connector:$it")) }
}
