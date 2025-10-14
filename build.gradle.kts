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
    implementation(libs.hotlibs.logging)
    implementation(libs.slf4j.api)
    implementation(libs.bundles.logging.runtime)

    // TokenX + AzureAD
    implementation(libs.tokendings.exchange)
    implementation(libs.tokenx.validation)
    implementation(libs.tokenx.validation.mock)
    implementation(libs.azure.validation)
    implementation(libs.azure.exchange)

    // Testing
    testImplementation(libs.bundles.ktor.server.test)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
}


kotlin { jvmToolchain(21) }

tasks.test {
    useJUnitPlatform()
    // Sikre at tester bruker samme tidssone som appen (ref. Dockerfile). E.g. Github Runners bruker UTC som default.
    systemProperty("user.timezone", "Europe/Oslo")
}
tasks.shadowJar {
    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}