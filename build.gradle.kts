group = "no.nav.hjelpemidler"
version = "1.0-SNAPSHOT"

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

application {
    applicationName = "hm-delbestilling-api"
    mainClass.set("no.nav.hjelpemidler.delbestilling.ApplicationKt")
}

dependencies {
    implementation(libs.kotlin.stdlib)

    // Database
    implementation(libs.hotlibs.database)
    implementation(libs.hotlibs.database) {
        capabilities {
            requireCapability("no.nav.hjelpemidler:database-postgresql")
        }
    }

    // Cache
    implementation(libs.cache.api)
    implementation(libs.caffeine)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")

    // Kafka
    implementation(libs.kafka.clients)

    // hm-http
    implementation(libs.hotlibs.http)

    // Ktor
    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.rate.limit)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.utils)

    // Jackson
    implementation(libs.bundles.jackson)

    // Logging
    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.api)
    implementation(libs.bundles.logging.runtime)

    // Microsoft Graph
    // TODO flytt til hotlibs
    implementation("com.microsoft.graph:microsoft-graph:5.77.0") // TODO bump me https://mvnrepository.com/artifact/com.microsoft.graph/microsoft-graph
    implementation("com.azure:azure-identity:1.12.2") // TODO bump me https://mvnrepository.com/artifact/com.azure/azure-identity

    // Microsoft Graph
    // TODO flytt til hotlibs?
    //implementation("com.microsoft.graph:microsoft-graph:6.38.0")
    //implementation("com.azure:azure-identity:1.16.1")

    // TokenX + AzureAD
    implementation(libs.tokendings.exchange)
    implementation(libs.tokenx.validation)
    implementation(libs.tokenx.validation.mock)
    implementation(libs.azure.validation)
    implementation(libs.azure.exchange)

    // Utils
    implementation(libs.nocommons)

    // Testing
    testImplementation(libs.bundles.ktor.server.test)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
}


kotlin { jvmToolchain(21) }

tasks.test { useJUnitPlatform() }
tasks.shadowJar { mergeServiceFiles() }