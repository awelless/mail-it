dependencies {
    // Persistence spi modules.
    api(project(":api-key:api-key-spi-persistence"))
    api(project(":core:core-spi-persistence"))
    api(project(":id-generator:id-generator-spi-locking"))
    api(project(":template:template-spi-persistence"))

    api(project(":value-classes"))
    api("commons-dbutils:commons-dbutils")

    implementation("com.esotericsoftware:kryo")

    implementation("io.quarkus:quarkus-kotlin")

    testImplementation(project(":common-test"))
}
