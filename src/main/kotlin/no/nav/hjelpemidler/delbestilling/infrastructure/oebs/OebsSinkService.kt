package no.nav.hjelpemidler.delbestilling.infrastructure.oebs

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingSak
import no.nav.hjelpemidler.domain.person.Fødselsnummer

private val log = KotlinLogging.logger {}

class OebsSinkService(
    private val oebsSinkClient: OebsSinkClient
) {
    fun sendDelbestilling(
        sak: DelbestillingSak,
        brukersFnr: Fødselsnummer,
        innsendernavn: String,
    ) {
        log.info { "Sender delbestilling for saksnummer '${sak.saksnummer}'" }

        val artikler = sak.delbestilling.deler.map { Artikkel(it.del.hmsnr, it.antall) }
        val forsendelsesinfo = genererForsendelsesinfo(sak.delbestilling.levering, innsendernavn)

        return oebsSinkClient.sendDelbestilling(
            Ordre(
                brukersFnr = brukersFnr.value,
                saksnummer = sak.saksnummer.toString(),
                innsendernavn = innsendernavn,
                artikler = artikler,
                forsendelsesinfo = forsendelsesinfo,
            )
        )
    }
}