import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val spaceSdkVersion: String by project
val exposedVersion: String by project
val hikariVersion: String by project
val postgresqlDriverVersion: String by project

plugins {
    kotlin("jvm") version "1.8.20"
    id("io.ktor.plugin") version "2.3.0"
    id("com.avast.gradle.docker-compose") version "0.16.2"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/space/maven")
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    // Space SDK
    implementation("org.jetbrains:space-sdk-jvm:$spaceSdkVersion")
    // Exposed is a DB access library by JetBrains
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    // HikariCP is a JDBC connection pool library
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    // PostgreSQL JDBC driver
    implementation("org.postgresql:postgresql:$postgresqlDriverVersion")
    // Adds ability to respond in HTML
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
}

// make `run` task dependent on `dockerComposeUp` task
tasks {
    val run by getting(JavaExec::class)
    dockerCompose.isRequiredBy(run)
}

// ensure the compiled code is compatible with Java 11 (Space SDK requirement)
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
