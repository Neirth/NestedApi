package io.neirth.nestedapi.authentication.util

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.SignatureException
import java.security.Key
import java.util.logging.Logger
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.login.LoginException
import javax.xml.bind.DatatypeConverter

val signingKey : Key = SecretKeySpec(
    DatatypeConverter.parseBase64Binary(System.getenv("LOGIN_KEY")),
    SignatureAlgorithm.HS512.jcaName
)

val loggerSystem: Logger = Logger.getLogger("Authentication Module")

fun processJwtToken(jwtToken : String?) : Map<String, Any?> {
    try {
        if (jwtToken != null) {
            return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(jwtToken).body
        } else {
            throw LoginException("The authorization key is not present")
        }
    } catch (e: ExpiredJwtException) {
        throw LoginException("The token has expired, please renew it before submitting another request")
    } catch (e: SignatureException) {
        throw SecurityException("The token has been tampered, please get a new valid token")
    }
}