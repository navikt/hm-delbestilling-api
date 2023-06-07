package no.nav.hjelpemidler.delbestilling.exceptions

class PersonNotFoundInPdl(message: String) : RuntimeException(message)

class PersonNotAccessibleInPdl(message: String = "") : RuntimeException(message)

class PdlRequestFailedException(message: String = "") : RuntimeException("Request til PDL feilet $message")

class PdlResponseMissingData(message: String = "") : RuntimeException("Response from PDL mangler nødvendig data $message")
