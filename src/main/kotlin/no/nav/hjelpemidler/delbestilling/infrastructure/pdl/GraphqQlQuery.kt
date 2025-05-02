package no.nav.hjelpemidler.delbestilling.infrastructure.pdl

fun hentKommunenummerQuery(fnummer: String) = GraphqlQuery(
    hentQuery("hentKommunenummer"),
    IdentVariables(ident = fnummer),
)

fun hentPersonNavnQuery(fnummer: String) = GraphqlQuery(
    hentQuery("hentPersonNavn"),
    IdentVariables(ident = fnummer),
)

private fun hentQuery(navn: String) =
    GraphqlQuery::class.java.getResource("/pdl/$navn.graphql")!!.readText()
        .replace("[\n\r]", "").replace("[\n]", "")

data class GraphqlQuery(
    val query: String,
    val variables: Variables,
)

interface Variables

data class IdentVariables(
    val ident: String,
) : Variables
