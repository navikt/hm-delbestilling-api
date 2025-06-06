package no.nav.hjelpemidler.delbestilling.delbestilling.anmodning

interface AnmodningDao {

    fun lagreAnmodninger(rapport: Anmodningrapport)

}