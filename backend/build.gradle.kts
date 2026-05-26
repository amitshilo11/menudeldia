// TODO: review dependency versions periodically; aligned with libs.versions.toml where possible.
//       Spring Boot manages most transitive versions via the BOM.

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.spring") version "2.3.21"
    kotlin("plugin.jpa") version "2.3.21"
    application
}

group = "com.menudeldia"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("com.menudeldia.MenuDelDiaApplicationKt")
}

dependencies {
    // --- Spring Boot core ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // --- Kotlin support ---
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(libs.kotlinx.serialization.json)

    // --- Database / migrations ---
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // --- PostGIS / geometry mapping ---
    implementation("org.hibernate.orm:hibernate-spatial")
    implementation("org.locationtech.jts:jts-core:1.20.0")

    // --- Auth / JWT ---
    // TODO Phase 3: enable when implementing sign-in
    implementation("com.google.api-client:google-api-client:2.7.0")
    implementation("com.nimbusds:nimbus-jose-jwt:9.40")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // --- Caching / resilience ---
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.2.0")

    // --- Rate limiting ---
    implementation("com.bucket4j:bucket4j_jdk17-core:8.14.0")

    // --- Metrics ---
    implementation("io.micrometer:micrometer-registry-prometheus")

    // --- Test ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.testcontainers:postgresql:1.20.4")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

fun loadEnv(): Map<String, String> {
    val envMap = mutableMapOf<String, String>()
    val envFile = project.file(".env")
    if (envFile.exists()) {
        envFile.forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                val (key, value) = trimmed.split("=", limit = 2)
                envMap[key.trim()] = value.trim()
            }
        }
    }
    return envMap
}

tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    systemProperty("spring.profiles.active", project.properties["profiles"] ?: "dev")

    val env = loadEnv()
    environment(
        "GOOGLE_PLACES_API_KEY",
        System.getenv("GOOGLE_PLACES_API_KEY") ?: env["GOOGLE_PLACES_API_KEY"] ?: ""
    )
    environment(
        "GOOGLE_OAUTH_CLIENT_ID",
        System.getenv("GOOGLE_OAUTH_CLIENT_ID") ?: env["GOOGLE_OAUTH_CLIENT_ID"] ?: ""
    )
    environment("JWT_SIGNING_KEY", System.getenv("JWT_SIGNING_KEY") ?: env["JWT_SIGNING_KEY"] ?: "")

    // Also pass DB config from .env if present
    env["DB_URL"]?.let { environment("DB_URL", it) }
    env["DB_USER"]?.let { environment("DB_USER", it) }
    env["DB_PASSWORD"]?.let { environment("DB_PASSWORD", it) }
    env["PHOTOS_DIR"]?.let { environment("PHOTOS_DIR", it) }
    env["ADMIN_TOKEN"]?.let { environment("ADMIN_TOKEN", it) }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}
