package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

fun valider(response: PdlPersonResponse): PdlPersonResponse {

    if (response.errors.isNotEmpty()) {
        val feilmeldinger = feilmeldinger(response)
        if (feilType(response) == PdlFeiltype.IKKE_FUNNET) {
            throw PersonNotFoundInPdl("Fant ikke person i PDL $feilmeldinger")
        } else {
            throw PdlRequestFailedException(feilmeldinger)
        }
    } else if (harDiskresjonskode(response)) {
        throw PersonNotAccessibleInPdl()
    }

    return response
}

private fun feilType(response: PdlPersonResponse): PdlFeiltype {
    return if (response.errors.map { it.extensions.code }.contains("not_found")) {
        PdlFeiltype.IKKE_FUNNET
    } else {
        PdlFeiltype.TEKNISK_FEIL
    }
}

private fun harDiskresjonskode(response: PdlPersonResponse): Boolean {
    val adressebeskyttelser = response.data?.hentPerson?.adressebeskyttelse ?: emptyList()
    return adressebeskyttelser.any {
        it.erKode6() || it.erKode7()
    }
}

private fun feilmeldinger(response: PdlPersonResponse): String {
    return response.errors.joinToString(",") { "${it.message}. Type ${it.extensions.classification}:${it.extensions.code}" }
}

enum class PdlFeiltype {
    IKKE_FUNNET,
    TEKNISK_FEIL,
}