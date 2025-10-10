package no.nav.hjelpemidler.delbestilling.testdata.fixtures

import no.nav.hjelpemidler.delbestilling.common.Delbestilling
import no.nav.hjelpemidler.delbestilling.common.Hmsnr
import no.nav.hjelpemidler.delbestilling.delbestilling.BestillerType
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingRequest
import no.nav.hjelpemidler.delbestilling.delbestilling.DelbestillingResultat
import no.nav.hjelpemidler.delbestilling.infrastructure.roller.Delbestiller
import no.nav.hjelpemidler.delbestilling.testdata.TestContext
import no.nav.hjelpemidler.delbestilling.testdata.Testdata
import no.nav.hjelpemidler.delbestilling.testdata.delLinje
import no.nav.hjelpemidler.delbestilling.testdata.delbestillerRolle
import no.nav.hjelpemidler.delbestilling.testdata.delbestilling
import no.nav.hjelpemidler.delbestilling.testdata.delbestillingRequest
import no.nav.hjelpemidler.delbestilling.testdata.organisasjon
import java.time.LocalDateTime

suspend fun TestContext.gittDelbestilling(
    delbestilling: Delbestilling = delbestilling(),
    dagerSidenOpprettelse: Long? = null
) {
    transaction(returnGeneratedKeys = true) {
        val saksnummer = delbestillingRepository.lagreDelbestilling(
            bestillerFnr = Testdata.fnr,
            brukerFnr = Testdata.fnr,
            brukerKommunenr = Testdata.defaultKommunenummer,
            delbestilling = delbestilling,
            brukersKommunenavn = Testdata.defaultKommunenavn,
            bestillersOrganisasjon = organisasjon(),
            bestillerType = BestillerType.KOMMUNAL,
        )

        if (dagerSidenOpprettelse != null) {
            val opprettet = LocalDateTime.now().minusDays(dagerSidenOpprettelse)
            delbestillingRepository.tx.update(
                sql = """
            UPDATE delbestilling
            SET opprettet = :opprettet
            WHERE saksnummer = :saksnummer
        """.trimIndent(),
                queryParameters = mapOf(
                    "opprettet" to opprettet,
                    "saksnummer" to saksnummer,
                )
            )
        }
    }
}

suspend fun TestContext.opprettDelbestilling(
    request: DelbestillingRequest = delbestillingRequest(),
    fnrBestiller: String = Testdata.fnr,
    rolle: Delbestiller = delbestillerRolle()
): DelbestillingResultat {
    return delbestillingService.opprettDelbestilling(request, fnrBestiller, rolle)
}

suspend fun TestContext.opprettDelbestillingMedDel(
    hmsnr: Hmsnr,
    antall: Int = 2,
): DelbestillingResultat {
    return opprettDelbestilling(
        request = delbestillingRequest(
            deler = listOf(
                delLinje(
                    hmsnr = hmsnr,
                    antall = antall
                )
            )
        )
    )
}

suspend fun TestContext.opprettDelbestillingMedDeler(
    vararg hmsnrs: Hmsnr,
): DelbestillingResultat {
    return opprettDelbestilling(
        request = delbestillingRequest(
            deler = hmsnrs.map { hmsnr -> delLinje(hmsnr = hmsnr) }
        )
    )
}