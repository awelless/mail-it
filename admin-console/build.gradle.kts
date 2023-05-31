import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("org.jetbrains.kotlin.plugin.allopen")
}

dependencies {
    implementation(project(":core:admin-api"))

    implementation("io.github.microutils:kotlin-logging-jvm")

    implementation("io.quarkus:quarkus-config-yaml")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-security")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
}

tasks.processResources {
    dependsOn("copyWebUiArtifactToResources")
}

task<Copy>("copyWebUiArtifactToResources") {
    dependsOn("buildWebUiArtifact")

    from("../admin-console-ui/dist/spa")
    into("src/main/resources/META-INF/resources")
}

val npmCommand = if (Os.isFamily(Os.FAMILY_WINDOWS)) "npm.cmd" else "npm"

task<Exec>("installWebUiDependencies") {
    workingDir("../admin-console-ui")

    commandLine(npmCommand, "install")
}

task<Exec>("buildWebUiArtifact") {
    dependsOn("installWebUiDependencies")

    workingDir("../admin-console-ui")

    commandLine(npmCommand, "run", "clean")
    commandLine(npmCommand, "run", "build")
}

tasks.clean {
    dependsOn("cleanWebUiArtifactFromResources")
}

task<Delete>("cleanWebUiArtifactFromResources") {
    delete("src/main/resources/META-INF/resources")
}
