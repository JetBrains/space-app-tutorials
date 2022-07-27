import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val space_sdk_version: String by project
val ktor_version: String by project
val logback_version: String by project
val exposed_version: String by project
val hikari_version: String by project
val postgresql_driver_version: String by project

plugins {
    kotlin("jvm") version "1.7.10"
    id("docker-compose")
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/space/maven")
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-jetty-jvm:$ktor_version")

    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")

    implementation("org.jetbrains:space-sdk-jvm:$space_sdk_version")

    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("com.zaxxer:HikariCP:$hikari_version")
    implementation("org.postgresql:postgresql:$postgresql_driver_version")

    testImplementation(kotlin("test"))
}

dockerCompose {
    projectName = "space-events"
    removeContainers = false
    removeVolumes = false
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    val run by getting(JavaExec::class)
    dockerCompose.isRequiredBy(run)
}

application {
    mainClass.set("io.ktor.server.jetty.EngineMain")
}
