package io.neirth.nestedapi.authentication.controller

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.neirth.nestedapi.authentication.domain.AuthSuccess
import io.neirth.nestedapi.authentication.exception.LoginException
import io.neirth.nestedapi.authentication.util.parseFormEncoded
import io.neirth.nestedapi.authentication.util.signingKey
import java.io.ByteArrayOutputStream
import java.net.MalformedURLException
import java.util.*
import javax.ws.rs.HeaderParam
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/auth")
class AuthCtrl {
    private val expirationTime : Long = System.getenv("EXPIRATION_TIME").toLong()

    @POST
    @Path("token")
    fun authToken(body: String): AuthSuccess {
        val requestVars : Map<String, String> = parseFormEncoded(body)

        return when {
            requestVars["grant_type"] == "password" -> {
                val username: String? = requestVars["username"]
                val password: String? = requestVars["password"]

                if (username != null && password != null) {
                    // TODO: Read user password throw the network
                    val userPass : String? = ""

                    if (userPass != null && userPass == password) {
                        val refreshToken : String = UUID.randomUUID().toString()

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

                    val actualTime = System.currentTimeMillis()
                    val expirationTime = System.currentTimeMillis() * expirationTime

                    val accessToken: String = Jwts.builder().setId(0.toString()).setSubject("")
                                                  .setExpiration(Date(expirationTime))
                                                  .setIssuedAt(Date(actualTime))
                                                  .signWith(signingKey, SignatureAlgorithm.HS512)
                                                  .compact()

                    AuthSuccess(accessToken, "bearer", expirationTime, refreshToken)
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
        val mapper = ObjectMapper(JsonFactory())
        val bsonFactory = BsonFactory()
        val output = ByteArrayOutputStream()

        mapper.writeTree(bsonFactory.createGenerator(output), mapper.readTree(body))

        // TODO: Send the petition throw the network and waits for the response
    }

    @POST
    @Path("logout")
    fun logoutUser(@HeaderParam("authorization") jwtToken: String) {
        TODO("Create a true logout function")
    }
}