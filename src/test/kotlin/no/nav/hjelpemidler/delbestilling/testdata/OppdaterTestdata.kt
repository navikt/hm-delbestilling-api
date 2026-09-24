package no.nav.hjelpemidler.delbestilling.testdata

import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.hjelpemidler.delbestilling.common.Kilde
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.Grunndata
import no.nav.hjelpemidler.delbestilling.infrastructure.grunndata.GrunndataClient
import no.nav.hjelpemidler.delbestilling.infrastructure.jsonMapper
import no.nav.hjelpemidler.delbestilling.oppslag.FinnDelerResultat
import no.nav.hjelpemidler.delbestilling.oppslag.FinnDelerTilHjelpemiddel
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnr2Hjm
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnrHjmTilHmsnrDeler
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnrTilDel
import no.nav.hjelpemidler.delbestilling.oppslag.legacy.data.hmsnrTilHjelpemiddelnavn
import java.io.File

fun main() {
    runBlocking {
        //oppdaterTestdata()
        //finnHjelpemiddelIGrunndataMenMedKunManuelleDeler()
        //finnHjelpemidlerIkkeFinnesIGrunndata()
        //finnDelerAlleredeIGrunndata()
        genererOppdatertHmsnrHjmTilHmsnrDeler()
        //genererOppdatertHmsnrTilHjelpemiddelnavn()
        //finnUbrukteDeler()
    }
}

private suspend fun oppdaterTestdata() {
    listOf("301993").forEach { lagreProduktOgDeler(it) }
}

private suspend fun lagreProduktOgDeler(hmsnr: String) {
    val client = client()
    val path = "src/test/resources/testdata/grunndata"

    val produktResponse = client.hentProdukt(hmsnr)
    val produktJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(produktResponse)
    File("$path/produkt_$hmsnr.json").writeText(produktJson)

    val produkt = produktResponse.produkt
    if (produkt != null) {
        val delerResponse = client.hentDeler(seriesId = produkt.seriesId, produktId = produkt.id)
        val delerJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(delerResponse)
        File("$path/deler_${produkt.seriesId}_${produkt.id}.json")
            .writeText(delerJson)
    }
}

private suspend fun finnHjelpemiddelIGrunndataMenMedKunManuelleDeler() {
    val grunndata = Grunndata(client())
    val finnDelerTilHjelpemiddel = FinnDelerTilHjelpemiddel(grunndata, mockk(relaxed = true), mockk(relaxed = true))
    hmsnr2Hjm.keys.forEach {
        if (grunndata.hentProdukt(it) == null) {
            return@forEach
        }

        val foo = finnDelerTilHjelpemiddel(it)

        if (foo is FinnDelerResultat.IkkeFunnet || foo !is FinnDelerResultat.Funnet) {
            return@forEach
        }

        if (foo.hjelpemiddel.deler.all { it.kilde == Kilde.MANUELL_LISTE }) {
            println("${foo.hjelpemiddel} har deler bare i manuell liste")
            return
        }
    }
}

private suspend fun finnHjelpemidlerIkkeFinnesIGrunndata() {
    val grunndata = Grunndata(client())

    val manglerIGrunndata = hmsnr2Hjm.values.filter { grunndata.hentProdukt(it.hmsnr) == null }

    println("${manglerIGrunndata.size} av ${hmsnr2Hjm.size} hjelpemidler i manuell liste finnes ikke i grunndata:")
    manglerIGrunndata.forEach { println("${it.hmsnr} ${it.navn}") }
}

// For hvert hjelpemiddel i manuell liste: finn deler som allerede finnes koblet til det hjelpemiddelet i grunndata.
// Disse koblingene kan da fjernes fra manuell liste.
private suspend fun finnDelerAlleredeIGrunndata() {
    val grunndata = Grunndata(client())

    hmsnr2Hjm.values.forEach { manuell ->
        val produkt = grunndata.hentProdukt(manuell.hmsnr) ?: return@forEach
        if (!produkt.erHovedprodukt) return@forEach

        val hmsnrDelerGrunndata = grunndata.hentDeler(produkt.serieId, produkt.produktId).map { it.hmsArtNr }.toSet()
        val hmsnrDelerManuell = manuell.deler.map { it.hmsnr }.toSet()

        val alleredeIGrunndata = hmsnrDelerManuell.intersect(hmsnrDelerGrunndata)
        if (alleredeIGrunndata.isNotEmpty()) {
            println("${manuell.hmsnr} ${manuell.navn}: deler allerede koblet i grunndata: $alleredeIGrunndata")
        }
    }
}

// Printer en oppdatert versjon av hmsnrHjmTilHmsnrDeler der koblinger som allerede finnes i grunndata er fjernet.
// Hjelpemiddel der alle deler dekkes av grunndata blir utelatt helt (linjen kan da slettes fra manuell liste).
private suspend fun genererOppdatertHmsnrHjmTilHmsnrDeler() {
    val grunndata = Grunndata(client())

    println("val hmsnrHjmTilHmsnrDeler = mapOf<Hmsnr, Set<Hmsnr>>(")
    hmsnrHjmTilHmsnrDeler.forEach { (hmsnrHjm, hmsnrDeler) ->
        val produkt = grunndata.hentProdukt(hmsnrHjm)
        val hmsnrDelerGrunndata = if (produkt != null && produkt.erHovedprodukt) {
            grunndata.hentDeler(produkt.serieId, produkt.produktId).map { it.hmsArtNr }.toSet()
        } else {
            emptySet()
        }

        val gjenværendeDeler = hmsnrDeler - hmsnrDelerGrunndata

        if (gjenværendeDeler.isNotEmpty()) {
            val delerString = gjenværendeDeler.joinToString(", ") { "\"$it\"" }
            val kommentar = if (produkt == null) " // hjm finnes ikke i grunndata" else ""
            println("    \"$hmsnrHjm\" to setOf($delerString),$kommentar")
        }
    }
    println(")")
}

// Printer en oppdatert versjon av hmsnrTilHjelpemiddelnavn der oppføringer som ikke har en
// tilhørende oppføring i hmsnrHjmTilHmsnrDeler er fjernet.
private fun genererOppdatertHmsnrTilHjelpemiddelnavn() {
    println("val hmsnrTilHjelpemiddelnavn: Map<Hmsnr, Hjelpemiddelnavn> = listOf<Hjelpemiddelnavn>(")
    hmsnrTilHjelpemiddelnavn.values
        .filter { it.hmsnr in hmsnrHjmTilHmsnrDeler }
        .forEach {
            println("    Hjelpemiddelnavn(hmsnr = \"${it.hmsnr}\", navn = \"${it.navn}\", isoKode = \"${it.isoKode}\"),")
        }
    println(").associateBy { it.hmsnr }")
}

// Printer hmsnr for deler i hmsnrTilDel som ikke brukes som verdi i hmsnrHjmTilHmsnrDeler.
private fun finnUbrukteDeler() {
    val brukteHmsnrDeler = hmsnrHjmTilHmsnrDeler.values.flatten().toSet()
    val ubrukteDeler = hmsnrTilDel.keys - brukteHmsnrDeler

    println("Ubrukte deler:")
    ubrukteDeler.forEach { println(it) }
}

private fun client() = GrunndataClient(baseUrl = "https://finnhjelpemiddel.nav.no")



// Hmsnr som er bestilt med serienummer før vi åpnet for bestilling med brukernr
val hmsnrs = listOf("322256", "166711", "317522", "316401", "203941", "174726", "161570", "273371", "249779", "255505", "218353", "303699", "326574", "304600", "250029", "301645", "301996", "326530", "278342", "154689", "218350", "273009", "203491", "231908", "236373", "179332", "326570", "316148", "279774", "229803", "273121", "272716", "318632", "278337", "301866", "317525", "235192", "317577", "241482", "302004", "304603", "273055", "296325", "185149", "229790", "288844", "279773", "230364", "241479", "317549", "221393", "326569", "229796", "164558", "291255", "232803", "316162", "229797", "302008", "242662", "304584", "278344", "241454", "234576", "304587", "229789", "270590", "329672", "230424", "296163", "322250", "318728", "277701", "235430", "308943", "273093", "316160", "250993", "250566", "296322", "317617", "202814", "018959", "250028", "132943", "317578", "250468", "291251", "301993", "229762", "213042", "232814", "316150", "316405", "308942", "308940", "326548", "167576", "241447", "174222", "203509", "023626", "230539", "279775", "304580", "145674", "185144", "097556", "250994", "317511", "229899", "250568", "317505", "231681", "211366", "317529", "229889", "278341", "278339", "219408", "229794", "296167", "304586", "326568", "301573", "317626", "241466", "303701", "239440", "270591", "235227", "318622", "218351", "221387", "249574", "326546", "174728", "304597", "317613", "250031", "331325", "164820", "326551", "320153", "317607", "229795", "317569", "241463", "185146", "330079", "326577", "320062", "168840", "303705", "232742", "286648", "316151", "326572", "249783", "321214", "218355", "277060", "214969", "229761", "206108", "296321", "232766", "167575", "317518", "168835", "141636", "277014", "296159", "236661", "304602", "320161", "326531", "249780", "316391", "097557", "203490", "329912", "192208", "301997", "248590", "241467", "296320", "203102", "255800", "167624", "273372", "318686", "231700", "308937", "322257", "250990", "277697", "145668", "203508", "296145", "311642", "219407", "143335", "218342", "221389", "232510", "320053", "322252", "320135", "304585", "218341", "273095", "229892", "199199", "302005", "232348", "278338", "142267", "268821", "179330", "249785", "179182", "296317", "273368", "229788", "273001", "317627", "317523", "291275", "250042", "278343", "296309", "229772", "204367", "241472", "317532", "289636", "317547", "203489", "229771", "232584", "304598", "179346", "317546", "168848", "318533", "221408", "320142", "229780", "230270", "316146", "326547", "304594", "277028", "250465", "316164", "318764", "221415", "326532", "273064", "179348", "322251", "229786", "229779", "221391", "166850", "326563", "218343", "249554", "304601", "326876", "311641", "273075", "317510", "250044", "250466", "250027", "304579", "145669", "235139", "250467", "316158", "292483", "316165", "241475", "229763", "316161", "277049", "173712", "317542", "317509", "316163", "303703", "317517", "230006", "320154", "251617", "022497", "317544", "320130", "217370", "317536", "278346", "232503", "330077", "326567", "317585", "249746", "202812", "273063", "229798", "250988", "320112", "242661", "255799", "229881", "251739", "203488", "317548", "317543", "143215", "304588", "250470", "250471", "241477", "329914", "316155", "229787", "011215", "273374", "229891", "278345", "326552", "233072", "296161", "301998", "253230", "241481", "241483", "303704", "296316", "316154", "326878", "278336", "316152", "292555", "249419", "303706", "234536", "221407", "236958", "199200", "329913", "278335", "273084", "311864", "168834", "302007", "186867", "250043", "320121", "326566", "277699", "232815", "276997", "320122", "322150", "233086", "185147", "097558", "248980", "219406", "318440", "296149", "296143", "229770", "321208", "199138", "331326", "168160", "318560", "192118", "326808", "318561", "185145", "011217", "273065", "277042", "251746", "250991", "203479", "317584", "296144", "206107", "249786", "278340", "320175", "317545", "250464", "326565", "204372", "241476", "214967", "296162", "326555", "317538", "308936", "221406", "233085", "317524", "242660", "326543", "302006", "322249", "250565", "218352", "167625", "316419", "304595", "176396", "238378", "229801", "229760", "296164", "278331", "326549", "318630", "277007", "317515", "321209", "273083", "218340", "251745", "326874", "242663", "139412", "316149", "308941", "166412", "317611", "330070", "159629", "270147", "231364", "320152", "292448", "250030", "174727", "221405", "173748", "238379", "318668", "022490", "322254", "278333", "326877", "317521", "145670", "203494", "318689", "317516", "206660", "164560", "277693", "192209", "022491", "229782", "229755", "229791", "221413", "316159", "215265", "301994", "301995", "317609", "249782", "318558", "203492", "330076", "322253", "322146", "174729", "308935", "318774", "316157", "167574", "219409", "221414", "296166", "304596", "317504", "326541", "298542", "162799", "296319", "231363", "326544", "321211", "308934", "277000", "304599", "326545", "290053", "301295", "303698", "296324", "311643", "219402", "292843", "301867", "304593", "232573", "168832", "317535", "316153", "277021", "273006", "326875", "204368", "296318", "022492", "011216", "316421", "218339", "326564", "218354", "255798", "317572", "241465", "231378", "252583", "296153", "255617", "250564", "296323", "302003", "317610", "273054", "229883", "215765", "219405", "296146", "290242", "273066", "229890", "198757")

private suspend fun finnIsoKoder() {
    val grunndata = Grunndata(client())
    val isokoder = mutableSetOf<String>()
    for (hmsnr in hmsnrs) {
        val produkt = grunndata.hentProdukt(hmsnr)
        val iso = produkt?.isoKategori ?: "UKJENT"
        isokoder.add(iso)
    }

    println("ISO-koder:")
    println(isokoder)
}
