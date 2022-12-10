apply {
    plugin("io.quarkus")
}

dependencies {
    implementation(project(":core:spi"))
    implementation(project(":persistence:common"))
    implementation(project(":persistence:liquibase"))

    implementation("io.quarkus:quarkus-jdbc-h2")

    testImplementation(project(":persistence:tests"))
}
