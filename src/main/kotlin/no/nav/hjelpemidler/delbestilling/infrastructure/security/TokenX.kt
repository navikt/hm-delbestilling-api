package no.nav.hjelpemidler.delbestilling.infrastructure.security

import io.ktor.server.application.ApplicationCall
import no.nav.tms.token.support.tokenx.validation.user.TokenXUserFactory

fun ApplicationCall.tokenXUser() = TokenXUserFactory.createTokenXUser(this)
