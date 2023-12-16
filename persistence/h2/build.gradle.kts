apply {
    plugin("io.quarkus")
}

dependencies {
    implementation(project(":core:spi"))
    implementation(project(":id-generator:id-generator-spi-locking"))
    implementation(project(":persistence:common"))
    implementation(project(":persistence:liquibase"))

    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-kotlin")

    implementation("io.quarkus:quarkus-jdbc-h2")

    testImplementation(project(":persistence:tests"))
}

tasks.getByName("quarkusGenerateCodeTests") {
    dependsOn("jandex")
}

tasks.getByName("quarkusDependenciesBuild") {
    dependsOn("jandex")
}
