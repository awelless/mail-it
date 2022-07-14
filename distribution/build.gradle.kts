dependencies {
    implementation(project(":admin-client"))

    implementation(project(":connector:http"))

    implementation(project(":domain:core"))

    // decide persistence module during build
    implementation(project(":persistence:h2"))
//    implementation(project(":persistence:postgresql"))

    testImplementation(project(":common-test"))
    testImplementation(project(":domain:admin-api"))
    testImplementation(project(":domain:external-api"))
    testImplementation(project(":persistence:api"))

    testImplementation("io.quarkus:quarkus-resteasy-reactive")
}
