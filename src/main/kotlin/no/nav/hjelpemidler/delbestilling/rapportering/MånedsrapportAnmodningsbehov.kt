package no.nav.hjelpemidler.delbestilling.rapportering

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.infrastructure.email.Email
import no.nav.hjelpemidler.delbestilling.infrastructure.persistence.transaction.Transaction
import java.time.YearMonth
import java.time.Clock

private val log = KotlinLogging.logger { }

const val MÅNEDSRAPPORT_ANMODNINGER_SUBJECT = "Oppsummering av anmodningsbehov for forrige måned"

class MånedsrapportAnmodningsbehov(
    private val transaction: Transaction,
    private val clock: Clock,
    private val email: Email,
) {

    suspend fun sendRapporterForForrigeMåned() {
        val forrigeMåned = YearMonth.now(clock).minusMonths(1)
        Lager.entries.forEach { lager ->
            sendRapport(lager, forrigeMåned)
        }
    }

    private suspend fun sendRapport(lager: Lager, måned: YearMonth) {
        log.info { "Starter månedsrapport om anmodninger for lager=$lager og måned=$måned" }
        val grunnlag = hentGrunnlag(lager, måned)

        if (grunnlag.anmodninger.isEmpty()) {
            log.info { "Lager $lager hadde ingen annmodninger. Avbryter." }
            return
        }

        val rapportTekst = fyllUtRapport(grunnlag)

        log.info { "Månedsrapport for $lager i $måned: $rapportTekst" }
        email.sendSimpleMessage(lager.epost(), MÅNEDSRAPPORT_ANMODNINGER_SUBJECT, rapportTekst)
    }

    suspend fun hentGrunnlag(lager: Lager, måned: YearMonth): Grunnlag {
        val anmodninger = transaction {
            anmodningDao.hentAnmodninger(lager, måned)
        }
        val aggregerteAnmodninger = anmodninger.groupBy { it.hmsnr }
            .map { (key, group) ->
                AggregertAnmodning(
                    antall = group.sumOf { it.antallAnmodet },
                    hmsnr = key,
                    navn = group.first().navn,
                    leverandør = group.first().leverandornavn
                )
            }
        val grunnlag = Grunnlag(lager, måned, aggregerteAnmodninger)

        log.info { "Hentet grunnlag for månedsrapportering: $grunnlag" }

        return grunnlag
    }

    fun fyllUtRapport(grunnlag: Grunnlag): String {

        var tekst = """
        Her er en oversikt over deler som er bestilt digitalt og måtte anmodes for
        Hjelpemiddelsentral: ${grunnlag.lager.navn} (${grunnlag.lager.nummer})
        Måned: ${grunnlag.måned}
        
        Hmsnr | Navn | Totalt antall anmodet | Leverandør
        
        """.trimIndent()

        grunnlag.anmodninger
            .sortedByDescending { it.antall }
            .forEach { anmodning ->
                tekst += "$anmodning \n"
            }


        return tekst
    }
}

data class Grunnlag(
    val lager: Lager,
    val måned: YearMonth,
    val anmodninger: List<AggregertAnmodning>,
)

data class AggregertAnmodning(
    val antall: Int,
    val hmsnr: Hmsnr,
    val navn: String,
    val leverandør: String,
) {
    override fun toString(): String {
        return "$hmsnr | $navn | $antall | $leverandør"
    }
}
