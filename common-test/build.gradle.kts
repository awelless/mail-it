val mockkVersion = "1.12.4" // https://mvnrepository.com/artifact/io.mockk/mockk
val restAssuredKotlinExtensionsVersion = "5.1.1" // https://mvnrepository.com/artifact/io.rest-assured/kotlin-extensions

dependencies {
    api(project(":domain:model"))

    api("io.mockk:mockk:$mockkVersion")
    api("io.quarkus:quarkus-junit5")
    api("io.rest-assured:rest-assured")
    api("io.rest-assured:kotlin-extensions:$restAssuredKotlinExtensionsVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-test")
}
