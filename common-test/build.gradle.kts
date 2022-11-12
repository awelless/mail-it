val jsoupVersion: String by project
val mockkVersion: String by project
val restAssuredKotlinExtensionsVersion: String by project

dependencies {
    api(project(":core:model"))

    api("io.mockk:mockk:$mockkVersion")
    api("io.quarkus:quarkus-junit5")
    api("io.rest-assured:rest-assured")
    api("io.rest-assured:kotlin-extensions:$restAssuredKotlinExtensionsVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    api("org.jsoup:jsoup:$jsoupVersion")
}
