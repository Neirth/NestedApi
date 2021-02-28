package io.neirth.nestedapi.users.util

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.SignatureException
import io.neirth.nestedapi.users.controller.UsersCtrl
import io.neirth.nestedapi.users.repository.LoginException
import java.util.logging.Logger

val loggerSystem: Logger = Logger.getLogger("Users Module")

fun processJwtToken(jwtToken : String?) : Map<String, Any?> {
    try {
        if (jwtToken != null) {
            return Jwts.parserBuilder().build().parseClaimsJws(jwtToken).body
        } else {
            throw LoginException("The authorization key is not present")
        }
    } catch (e: ExpiredJwtException) {
        throw LoginException("The token has expired, please renew it before submitting another request")
    } catch (e: SignatureException) {
        throw SecurityException("The token has been tampered, please get a new valid token")
    }
}
