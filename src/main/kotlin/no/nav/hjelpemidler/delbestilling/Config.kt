package no.nav.hjelpemidler.delbestilling

import no.nav.hjelpemidler.configuration.EnvironmentVariable
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

    // Database
    val DB_HOST by EnvironmentVariable
    val DB_PORT by EnvironmentVariable
    val DB_DATABASE by EnvironmentVariable
    val DB_USERNAME by EnvironmentVariable
    val DB_PASSWORD by EnvironmentVariable

    val dataSource by lazy {
        createDataSource {
            hostname = DB_HOST
            port = DB_PORT.toInt()
            database = DB_DATABASE
            username = DB_USERNAME
            password = DB_PASSWORD
            envVarPrefix = "DB"
        }
    }
}