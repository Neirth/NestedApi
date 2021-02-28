/*
 * MIT License
 *
 * Copyright (c) 2021 NestedApi Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.neirth.nestedapi.users.util

import java.security.Key
import java.util.logging.Logger
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.SignatureException
import io.neirth.nestedapi.users.exception.LoginException

val signingKey : Key = SecretKeySpec(
    DatatypeConverter.parseBase64Binary(System.getenv("LOGIN_KEY")),
    SignatureAlgorithm.HS512.jcaName
)

val loggerSystem: Logger = Logger.getLogger("Users Module")

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
