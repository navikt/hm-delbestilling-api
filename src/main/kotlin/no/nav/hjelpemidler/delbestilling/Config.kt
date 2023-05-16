package no.nav.hjelpemidler.delbestilling

import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.configuration.EnvironmentVariable
import no.nav.hjelpemidler.configuration.External
import no.nav.hjelpemidler.configuration.KafkaEnvironmentVariable
import no.nav.hjelpemidler.configuration.NaisEnvironmentVariable
import no.nav.hjelpemidler.database.createDataSource
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import java.util.Properties

object Config {

    // OEBS
    val OEBS_API_URL by EnvironmentVariable
    val OEBS_API_SCOPE by EnvironmentVariable

    // Roller
    val ROLLER_API_URL by EnvironmentVariable
    val ROLLER_API_SCOPE by EnvironmentVariable

    // Kafka
    val kafkaProducerProperties: Properties by lazy {
        when (NaisEnvironmentVariable.NAIS_CLUSTER_NAME) {
            "local" -> Properties().apply {
                put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, KafkaEnvironmentVariable.KAFKA_BROKERS)
                put(SaslConfigs.SASL_MECHANISM, "PLAIN")
                put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT")
            }

            else -> Properties().apply {
                put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, KafkaEnvironmentVariable.KAFKA_BROKERS)
                put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name)
                put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "")
                put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks")
                put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12")
                put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, KafkaEnvironmentVariable.KAFKA_TRUSTSTORE_PATH)
                put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, KafkaEnvironmentVariable.KAFKA_CREDSTORE_PASSWORD)
                put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, KafkaEnvironmentVariable.KAFKA_KEYSTORE_PATH)
                put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, KafkaEnvironmentVariable.KAFKA_CREDSTORE_PASSWORD)
            }
        }
    }

    val PDL_GRAPHQL_URL = "http://localhost:8089/pdl" // by EnvironmentVariable
    val PDL_API_SCOPE = "api://dev-gcp.pdl.pdl-api/.default" //by EnvironmentVariable

    // Database
    @External
    val DB_HOST by EnvironmentVariable

    @External
    val DB_PORT by EnvironmentVariable

    @External
    val DB_DATABASE by EnvironmentVariable

    @External
    val DB_USERNAME by EnvironmentVariable

    @External
    val DB_PASSWORD by EnvironmentVariable
}

fun isLocal() = Environment.current.tier.isLocal
fun isDev() = Environment.current.tier.isDev
fun isProd() = Environment.current.tier.isProd