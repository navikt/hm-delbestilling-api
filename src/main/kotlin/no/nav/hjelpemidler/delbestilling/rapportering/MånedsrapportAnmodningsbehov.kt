package no.nav.hjelpemidler.delbestilling.rapportering

import io.github.oshai.kotlinlogging.KotlinLogging
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.common.Lager
import no.nav.hjelpemidler.delbestilling.config.isDev
import no.nav.hjelpemidler.delbestilling.config.isProd
import no.nav.hjelpemidler.delbestilling.infrastructure.email.ContentType
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
        if (!isProd()) {
            // TODO Skru på utsending i prod når alt er klart
            email.send(lager.epost(), MÅNEDSRAPPORT_ANMODNINGER_SUBJECT, rapportTekst, ContentType.HTML)
        }
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
        val anmodningRader = grunnlag.anmodninger
            .sortedByDescending { it.antall }
            .joinToString("") { anmodning ->
                """
                    <tr>
                        <td>${anmodning.hmsnr}</td>
                        <td>${anmodning.navn}</td>
                        <td>${anmodning.antall}</td>
                        <td>${anmodning.leverandør}</td>
                    </tr>
                """.trimIndent()
            }

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>$MÅNEDSRAPPORT_ANMODNINGER_SUBJECT</title>
                <style>
                    table {
                        width: 100%;
                        border-collapse: collapse;
                    }
                    th, td {
                        padding: 10px;
                        border: 1px solid #ccc;
                        text-align: left;
                    }
                </style>
            </head>
            <body>
                <p>
                    Her er en oversikt over deler som er bestilt digitalt og måtte anmodes for </br> 
                    HMS lager: ${grunnlag.lager.navn} </br>
                    Måned: ${grunnlag.måned}
                </p>
                <table>
                    <thead>
                        <tr>
                            <th>Hmsnr</th>
                            <th>Navn</th>
                            <th>Antall</th>
                            <th>Leverandør</th>
                        </tr>
                    </thead>
                    <tbody>
                        $anmodningRader
                    </tbody>
                </table>
            </body>
            </html>
        """.trimIndent()




        return html
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
