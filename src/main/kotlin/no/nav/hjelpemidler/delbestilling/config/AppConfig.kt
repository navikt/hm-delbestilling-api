package no.nav.hjelpemidler.delbestilling.config

import no.nav.hjelpemidler.configuration.Environment
import no.nav.hjelpemidler.configuration.EnvironmentVariable
import no.nav.hjelpemidler.configuration.KafkaEnvironmentVariable
import no.nav.hjelpemidler.configuration.NaisEnvironmentVariable
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import java.util.Properties

object AppConfig {

    // OEBS
    val OEBS_API_URL by EnvironmentVariable
    val OEBS_API_SCOPE by EnvironmentVariable

    // Grunndata
    val GRUNNDATA_API_URL by EnvironmentVariable

    // Norg
    val NORG_API_URL by EnvironmentVariable

    // Roller
    val ROLLER_API_URL by EnvironmentVariable
    val ROLLER_API_SCOPE by EnvironmentVariable

    // Oppslag
    val OPPSLAG_API_URL by EnvironmentVariable

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

    // PDL
    val PDL_GRAPHQL_URL by EnvironmentVariable
    val PDL_API_SCOPE by EnvironmentVariable

    // Azure
    val AZURE_APP_CLIENT_ID by EnvironmentVariable
    val AZURE_APP_CLIENT_SECRET by EnvironmentVariable
    val AZURE_APP_TENANT_ID by EnvironmentVariable
    val AZURE_APP_JWK by EnvironmentVariable
    val AZURE_OPENID_CONFIG_ISSUER by EnvironmentVariable
    val AZURE_OPENID_CONFIG_TOKEN_ENDPOINT by EnvironmentVariable

    // Epost
    val EPOST_AVSENDER by EnvironmentVariable
}

fun isLocal() = Environment.current.tier.isLocal
fun isDev() = Environment.current.tier.isDev
fun isProd() = Environment.current.tier.isProd
