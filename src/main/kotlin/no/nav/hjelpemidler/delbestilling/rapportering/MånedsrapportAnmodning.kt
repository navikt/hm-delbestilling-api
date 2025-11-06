package no.nav.hjelpemidler.delbestilling.rapportering

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.delbestilling.anmodning.AnmodningDao
import java.time.YearMonth

private val log = KotlinLogging.logger { }

/**
 * Sender rapport til gitt lager om hvilke deler (summert antall) som har blitt anmodet siste måned.
 */
class MånedsrapportAnmodning(
    private val anmodningDao: AnmodningDao
) {

    fun sendRapport(lager: Lager, måned: YearMonth) {
        log.info { "Starter månedsrapport om anmodninger for lager $lager og måned=$måned" }
        val grunnlag = hentGrunnlag(lager, måned)
        val rapportTekst = fyllUtRapport(grunnlag)

    }

    fun hentGrunnlag(lager: Lager, måned: YearMonth): Grunnlag {
        val anmodninger = anmodningDao.hentAnmodninger(lager, måned)
            .groupBy { it.hmsnr }
            .map { (key, group) ->
                AggregertAnmodning(
                    antall = group.sumOf { it.antallAnmodet },
                    hmsnr = key,
                    navn = group.first().navn,
                    leverandør = group.first().leverandornavn
                )
            }
        val grunnlag = Grunnlag(lager, måned, anmodninger)

        log.info { "Hentet grunnlag for månedsrapportering: $grunnlag" }

        return grunnlag
    }

    fun fyllUtRapport(grunnlag: Grunnlag): String {
        return """
        Her er en oversikt over deler som er bestilt digitalt og måtte anmodes for
        Hjelpemiddelsentral: ${grunnlag.lager.navn} (${grunnlag.lager.nummer})
        Måned: ${grunnlag.måned}
        
        Hmsnr | Navn | Totalt antall anmodet | Leverandør
        """.trimIndent()
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
)