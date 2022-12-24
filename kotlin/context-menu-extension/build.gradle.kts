import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val spaceSdkVersion: String by project
val kotlinVersion: String by project
val ktorVersion: String by project
val logbackVersion: String by project
val exposedVersion: String by project
val hikariVersion: String by project
val postgresDriverVersion: String by project
val jacksonVersion: String by project

plugins {
    kotlin("jvm") version "1.7.21"
    id("docker-compose")
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/space/maven")

    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-jetty-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    implementation("org.jetbrains:space-sdk-jvm:$spaceSdkVersion")
    // Include jackson databinding to override transitive vulnerable version 2.13.1
    // TODO Remove once Space SDK updates dependency
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.postgresql:postgresql:$postgresDriverVersion")

    testImplementation(kotlin("test"))
}

kotlin.sourceSets.all {
    languageSettings {
        optIn("kotlin.time.ExperimentalTime")
        optIn("space.jetbrains.api.ExperimentalSpaceSdkApi")
    }
}

dockerCompose {
    // Docker Compose configuration was broken and not fixed.
    // See https://github.com/avast/gradle-docker-compose-plugin/issues/353
    setProjectName("space-events")
    removeContainers.set(false)
    removeVolumes.set(false)
}

tasks {
    val run by getting(JavaExec::class)
    dockerCompose.isRequiredBy(run)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("io.ktor.server.jetty.EngineMain")
}
