apply {
    plugin("io.quarkus")
}

val dbUtilsVersion: String by project
val kryoVersion: String by project

dependencies {
    api("commons-dbutils:commons-dbutils:$dbUtilsVersion")

    implementation("com.esotericsoftware:kryo:$kryoVersion")

    testImplementation(project(":common-test"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
