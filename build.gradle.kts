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
    implementation(platform(libs.hotlibs.platform))

    // Database
    implementation(libs.hotlibs.database) {
        capabilities {
            requireCapability("no.nav.hjelpemidler:database-postgresql")
        }
    }

    // Cache
    implementation(libs.cache.api)
    implementation(libs.caffeine)

    // Kafka
    implementation(libs.kafka.clients)

    // hotlibs/http
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
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

testing {
    @Suppress("UnstableApiUsage")
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest(libs.versions.kotlin.asProvider())

            dependencies {
                implementation(libs.hotlibs.test)
                implementation(libs.hotlibs.database) {
                    capabilities {
                        requireCapability("no.nav.hjelpemidler:database-testcontainers")
                    }
                }

                implementation(libs.ktor.client.mock)
                implementation(libs.ktor.server.test.host)
            }

            targets.all {
                testTask {
                    environment("NAIS_CLUSTER_NAME", "test")
                    systemProperty("user.timezone", "Europe/Oslo")
                }
            }
        }
    }
}

tasks.shadowJar {
    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
