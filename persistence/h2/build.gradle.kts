apply {
    plugin("io.quarkus")
}

dependencies {
    implementation(project(":persistence:api"))
    implementation(project(":persistence:common"))
    implementation(project(":persistence:liquibase"))

    implementation("io.quarkus:quarkus-agroal")
    implementation("io.quarkus:quarkus-jdbc-h2")

    testImplementation(project(":persistence:tests"))
}
