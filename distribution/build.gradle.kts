dependencies {
    implementation(project(":domain:core"))
//    implementation(project(":persistence:h2"))
    implementation(project(":persistence:postgresql"))

    testImplementation(project(":common-test"))
    testImplementation(project(":persistence:api"))
}
