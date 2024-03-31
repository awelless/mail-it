import io.quarkus.gradle.extension.QuarkusPluginExtension

apply {
    plugin("io.quarkus")
}

extensions.getByType<QuarkusPluginExtension>().apply {
    finalName.set("mail-it-${project.version}")
}

dependencies {
    compileOnly("org.freemarker:freemarker")
    compileOnly("org.graalvm.nativeimage:svm")

    implementation(project(":core:service")) // core implementation
    implementation(project(":admin-console")) // admin console implementation
    implementation(project(":api-key:api-key-quarkus"))
    implementation(project(":id-generator:id-generator-quarkus"))
    implementation(project(":template:template-quarkus"))
    implementation(project(":worker:worker-quarkus"))
    databaseProviderImplementation()
    connectorsImplementation()

    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-smallrye-health")

    testImplementation(project(":api-key:api-key-api"))
    testImplementation(project(":api-key:api-key-spi-persistence"))
    testImplementation(project(":common-test"))
    testImplementation(project(":core:admin-api"))
    testImplementation(project(":core:spi"))
    testImplementation(project(":worker:worker-spi-persistence"))

    testImplementation("io.quarkus:quarkus-resteasy-reactive")
}

tasks.getByName("quarkusGenerateCodeTests") {
    dependsOn("jandex")
}

tasks.getByName("quarkusDependenciesBuild") {
    dependsOn("jandex")
}

/**
 * Resolves database provider dependency, depending on property passed for task
 */
fun DependencyHandler.databaseProviderImplementation(): Dependency? {
    // database provider names should be the same as modules in :persistence
    val supportedDatabaseProviders = setOf("h2", "mysql", "postgresql")
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
