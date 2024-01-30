import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.hjelpemidler"
version = "1.0-SNAPSHOT"

plugins {
    application
    kotlin("jvm") version "1.8.0"
    id("io.ktor.plugin") version "2.3.7"
}

application {
    applicationName = "hm-delbestilling-api"
    mainClass.set("no.nav.hjelpemidler.delbestilling.ApplicationKt")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io") // Used for tms-ktor-token-support
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    // Database
    implementation("no.nav.hjelpemidler.database:hm-database:v0.0.20")

    // Cache
    implementation("javax.cache:cache-api:1.1.1")
    implementation("org.ehcache:ehcache:3.10.8")

    // Kafka
    implementation("org.apache.kafka:kafka-clients:3.6.0")

    // hm-http
    implementation("no.nav.hjelpemidler.http:hm-http:v0.0.30")

    // Ktor
    val ktorVersion = "2.3.7"
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    fun ktor(name: String) = "io.ktor:ktor-$name:$ktorVersion"
    implementation(ktor("serialization-jackson"))
    implementation(ktor("server-core"))
    implementation(ktor("server-cio"))
    implementation(ktor("server-auth"))
    implementation(ktor("server-content-negotiation"))
    implementation(ktor("server-status-pages"))
    implementation(ktor("server-rate-limit"))
    implementation(ktor("server-request-validation"))
    implementation(ktor("server-call-id"))
    implementation(ktor("server-call-logging-jvm"))

    implementation(ktor("client-core"))
    implementation(ktor("client-content-negotiation"))
    implementation(ktor("client-jackson"))

    // Jackson
    val jacksonVersion = "2.16.0"
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    // Logging
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.1")
    implementation("org.slf4j:slf4j-api:2.0.9") // brukes av microutils:kotling-logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // TokenX + AzureAD
    val tokenSupportVersion = "4.0.0"
    implementation("com.github.navikt.tms-ktor-token-support:tokendings-exchange:$tokenSupportVersion")
    implementation("com.github.navikt.tms-ktor-token-support:tokenx-validation:$tokenSupportVersion")
    implementation("com.github.navikt.tms-ktor-token-support:tokenx-validation-mock:$tokenSupportVersion")
    implementation("com.github.navikt.tms-ktor-token-support:azure-validation:$tokenSupportVersion")
    implementation("com.github.navikt.tms-ktor-token-support:azure-exchange:$tokenSupportVersion")

    // Utils
    implementation("no.bekk.bekkopen:nocommons:0.15.0")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation(ktor("server-test-host"))
    val junitJupiterVersion = "5.10.1"
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation(ktor("client-mock"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("com.playtika.testcontainers:embedded-postgresql:3.0.6")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        showStandardStreams = true
        outputs.upToDateWhen { false }
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "7.4.2"
}

ktor {
    fatJar {
        archiveFileName.set("hm-delbestilling-api-fat.jar")
    }
}

tasks.named("buildFatJar") {
    dependsOn("test")
}

