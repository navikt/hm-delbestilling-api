package no.nav.hjelpemidler.delbestilling.fakes

import no.nav.hjelpemidler.delbestilling.common.Enhet
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.Anmodningrapport
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.Del
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.DelUtenDekningDao
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

class FakeDelUtenDekningDao : DelUtenDekningDao {

    private val idCounter = AtomicLong(1L)

    data class DelEntry(
        val id: Long,
        val saksnummer: Long,
        val hmsnr: Hmsnr,
        val navn: String,
        val antallUtenDekning: Int,
        val brukersKommunenummer: String,
        val brukersKommunenavn: String,
        val enhetnr: String,
        var rapportertTidspunkt: LocalDateTime? = null,
        var sistOppdatert: LocalDateTime = LocalDateTime.now()
    )

    private val delerUtenDekning = mutableListOf<DelEntry>()

    override fun lagreDelerUtenDekning(
        saksnummer: Long,
        hmsnr: Hmsnr,
        navn: String,
        antallUtenDekning: Int,
        bukersKommunenummer: String,
        brukersKommunenavn: String,
        enhetnr: String
    ): Long {
        val id = idCounter.getAndIncrement()
        delerUtenDekning += DelEntry(
            id = id,
            saksnummer = saksnummer,
            hmsnr = hmsnr,
            navn = navn,
            antallUtenDekning = antallUtenDekning,
            brukersKommunenummer = bukersKommunenummer,
            brukersKommunenavn = brukersKommunenavn,
            enhetnr = enhetnr
        )
        return id
    }

    override fun hentUnikeEnheter(): List<Enhet> =
        delerUtenDekning
            .filter { it.rapportertTidspunkt == null }
            .map { Enhet.fraEnhetsnummer(it.enhetnr) }
            .distinct()

    override fun hentDelerTilRapportering(enhetnr: String): List<Del> =
        delerUtenDekning
            .filter { it.enhetnr == enhetnr && it.rapportertTidspunkt == null }
            .groupBy { it.hmsnr to it.navn }
            .map { (key, entries) ->
                val (hmsnr, navn) = key
                Del(hmsnr = hmsnr, navn = navn, antall = entries.sumOf { it.antallUtenDekning })
            }

    override fun markerDelerSomRapportert(enhet: Enhet) {
        val now = LocalDateTime.now()
        delerUtenDekning
            .filter { it.enhetnr == enhet.nummer && it.rapportertTidspunkt == null }
            .forEach {
                it.rapportertTidspunkt = now
                it.sistOppdatert = now
            }
    }

    override fun markerDelerSomIkkeRapportert() {
        delerUtenDekning.forEach {
            it.rapportertTidspunkt = null
            it.sistOppdatert = LocalDateTime.now()
        }
    }
}