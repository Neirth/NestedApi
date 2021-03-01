package io.neirth.nestedapi.authentication.controller

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.neirth.nestedapi.authentication.domain.AuthSuccess
import io.neirth.nestedapi.authentication.domain.Credential
import io.neirth.nestedapi.authentication.domain.RefreshToken
import io.neirth.nestedapi.authentication.exception.LoginException
import io.neirth.nestedapi.authentication.repository.CredentialsRepo
import io.neirth.nestedapi.authentication.repository.RefreshTokenRepo
import io.neirth.nestedapi.authentication.util.annotation.RpcMessage
import io.neirth.nestedapi.authentication.util.parseFormEncoded
import io.neirth.nestedapi.authentication.util.sendMessage
import io.neirth.nestedapi.authentication.util.signingKey
import java.net.MalformedURLException
import java.sql.Timestamp
import java.util.*
import javax.inject.Inject
import javax.ws.rs.HeaderParam
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/auth")
class AuthCtrl {
    @Inject
    internal lateinit var connRefresh: RefreshTokenRepo

    @Inject
    internal lateinit var conCredential: CredentialsRepo

    private val expirationTime: Long = System.getenv("EXPIRATION_TIME").toLong()

    @POST
    @Path("token")
    fun authToken(body: String): AuthSuccess {
        val requestVars: Map<String, String> = parseFormEncoded(body)

        return when {
            requestVars["grant_type"] == "password" -> {
                val username: String? = requestVars["username"]
                val password: String? = requestVars["password"]

                if (username != null && password != null) {
                    // TODO: Read user password throw the network
                    val userPass: String? = ""

                    if (userPass != null && userPass == password) {
                        val refreshToken: String = UUID.randomUUID().toString()

                        val actualTime = System.currentTimeMillis()
                        val expirationTime = System.currentTimeMillis() * expirationTime

                        // TODO: Save the token into the database

                        val accessToken: String = Jwts.builder().setId(0.toString()).setSubject("")
                            .setExpiration(Date(expirationTime))
                            .setIssuedAt(Date(actualTime))
                            .signWith(signingKey, SignatureAlgorithm.HS512)
                            .compact()

                        AuthSuccess(accessToken, "bearer", expirationTime, refreshToken)
                    } else {
                        throw LoginException("The username or the password is not validated")
                    }
                } else {
                    throw MalformedURLException("The body is malformed")
                }
            }

            requestVars["grant_type"] == "refresh_token" -> {
                val refreshToken: String? = requestVars["refresh_token"]

                if (refreshToken != null) {
                    // TODO: Recover the token from the database
                    // TODO: Read user info throw the network
                    val objectMapper = ObjectMapper()
                    val jsonRequest: JsonParser = objectMapper.createParser("{ \"id\": \"\"")
                    val user: JsonNode? = sendMessage("users.login", objectMapper.readTree(jsonRequest))

                    if (user != null) {
                        val actualTime = System.currentTimeMillis()
                        val expirationTime = System.currentTimeMillis() * expirationTime

                        val accessToken: String = Jwts.builder().setId(0.toString()).setSubject("")
                            .setExpiration(Date(expirationTime))
                            .setIssuedAt(Date(actualTime))
                            .signWith(signingKey, SignatureAlgorithm.HS512)
                            .compact()

                        AuthSuccess(accessToken, "bearer", expirationTime, refreshToken)
                    } else {
                        throw LoginException("No couldn't validated the user")
                    }
                } else {
                    throw MalformedURLException("The body is malformed")
                }
            }

            else -> {
                throw MalformedURLException("The grant_type option isn't supported")
            }
        }
    }

    @POST
    @Path("register")
    fun registerUser(body: String) {
        val objectMapper = ObjectMapper()
        val jsonRequest: JsonParser = objectMapper.createParser(body)

        sendMessage("users.register", objectMapper.readTree(jsonRequest))
            ?: throw MalformedURLException("The body is malformed")
    }

    @POST
    @Path("logout")
    fun logoutUser(@HeaderParam("authorization") jwtToken: String) {
        TODO("Create a true logout function")
    }

    @RpcMessage(topic = "auths", queue = "remove")
    fun deleteCredentials(credential: Credential): Credential {
        conCredential.remove(credential)
        connRefresh.remove(RefreshToken(credential.userId, "", Timestamp(0)))

        return credential
    }
}
