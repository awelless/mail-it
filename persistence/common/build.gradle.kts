val dbUtilsVersion = "1.7" // https://mvnrepository.com/artifact/commons-dbutils/commons-dbutils
val kryoVersion = "5.3.0" // https://mvnrepository.com/artifact/com.esotericsoftware/kryo

dependencies {
    api("commons-dbutils:commons-dbutils:$dbUtilsVersion")

    implementation("com.esotericsoftware:kryo:$kryoVersion")

    testImplementation(project(":common-test"))
}
