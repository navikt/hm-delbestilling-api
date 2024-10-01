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
    implementation(libs.ehcache)

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

    // Jackson
    implementation(libs.bundles.jackson)

    // Logging
    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.api)
    implementation(libs.bundles.logging.runtime)

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