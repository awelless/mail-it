apply {
    plugin("io.quarkus")
}

dependencies {
    implementation(project(":persistence:common"))
    implementation(project(":persistence:liquibase"))

    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-kotlin")

    implementation("io.quarkus:quarkus-jdbc-mysql")
    implementation("io.quarkus:quarkus-reactive-mysql-client")

    implementation("io.smallrye.reactive:mutiny-kotlin")

    testImplementation(project(":persistence:tests"))
    testImplementation("io.quarkus:quarkus-agroal")
}

tasks.getByName("quarkusGenerateCodeTests") {
    dependsOn("jandex")
}

tasks.getByName("quarkusDependenciesBuild") {
    dependsOn("jandex")
}
