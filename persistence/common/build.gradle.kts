apply {
    plugin("io.quarkus")
}

dependencies {
    api("commons-dbutils:commons-dbutils")

    implementation(project(":core:model"))
    implementation("com.esotericsoftware:kryo")

    implementation("io.quarkus:quarkus-kotlin")

    testImplementation(project(":common-test"))
}

tasks.getByName("quarkusGenerateCodeTests") {
    dependsOn("jandex")
}
