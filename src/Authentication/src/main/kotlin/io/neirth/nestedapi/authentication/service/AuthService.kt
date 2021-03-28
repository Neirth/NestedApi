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
package io.neirth.nestedapi.authentication.service

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.neirth.nestedapi.authentication.domain.response.AuthSuccess
import io.neirth.nestedapi.authentication.domain.Credential
import io.neirth.nestedapi.authentication.domain.RefreshToken
import io.neirth.nestedapi.authentication.exception.LoginException
import io.neirth.nestedapi.authentication.repository.CredentialsRepo
import io.neirth.nestedapi.authentication.repository.RefreshTokenRepo
import io.neirth.nestedapi.authentication.util.RpcUtils
import io.neirth.nestedapi.authentication.util.sendEmail
import io.neirth.nestedapi.authentication.util.signingKey
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hibernate.exception.DataException
import java.net.MalformedURLException
import java.sql.Timestamp
import java.util.*
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class AuthService(private val connRefresh: RefreshTokenRepo, private val connCredential: CredentialsRepo) {
    @ConfigProperty(name = "nestedapi.expiration.time")
    internal lateinit var expirationTimeNumber: String

    /**
     * Method for obtain the refresh token and the access token
     *
     * @param requestVars The request values
     * @return The encapsulated response
     */
    fun tokenGrantPasswd(requestVars: Map<String, String>): AuthSuccess {
        // Retrieve the username and password
        val username: String? = requestVars["username"]
        val password: String? = requestVars["password"]

        // Check if the username and the password isn't null
        if (username != null && password != null) {
            // Find the username in the database
            val credential = connCredential.findByUsername(username)

            // Check if the credential is found
            if (credential != null) {
                // Check if the password is the same
                if (credential.password.trim() == password.trim()) {
                    // Generate a random UUID key
                    val refreshToken: String = UUID.randomUUID().toString()

                    // Determinate the actual time and the expiration time
                    val actualTime: Long = System.currentTimeMillis()
                    val expirationTime: Long = System.currentTimeMillis() + (expirationTimeNumber.toLong() * 60)

                    // Obtain the user info from users
                    val objectMapper = ObjectMapper()
                    val jsonRequest: JsonParser = objectMapper.createParser("{ \"id\": " + credential.userId + " }")
                    val user: JsonNode? = RpcUtils.sendMessage("users.login", objectMapper.readTree(jsonRequest))

                    // Check if the user isn't null
                    if (user != null) {
                        // Save the new refresh token
                        connRefresh.insert(RefreshToken(null, credential.userId, refreshToken, Timestamp(actualTime)))

                        // Get the access token
                        val accessToken: String =
                            Jwts.builder().setId(user["id"].asText()).setSubject(user["email"].asText())
                                .setExpiration(Date(expirationTime)).setIssuedAt(Date(actualTime))
                                .signWith(signingKey, SignatureAlgorithm.HS512).compact()

                        // Send the email to the user with the login warning
                        sendEmail(
                            user["email"].asText(),
                            "Login Detected",
                            "Login Detected",
                            "The login was detected, please check the login registry"
                        )

                        // return the AuthSuccess object to the http call
                        return AuthSuccess(accessToken, "bearer", expirationTimeNumber.toLong(), refreshToken)
                    } else {
                        // If the user in the users database is null, return no validate operation
                        throw LoginException("The username or the password is not validated")
                    }
                } else {
                    // If the password is not the same, return no validate operation
                    throw LoginException("The username or the password is not validated")
                }
            } else {
                // If the credential is no find, return no validate operation
                throw LoginException("The username or the password is not validated")
            }
        } else {
            // If the body is bad formed, return malformed url exception
            throw MalformedURLException("The body is malformed")
        }
    }

    /**
     * Method for obtain a new access token using the refresh token
     *
     * @param requestVars The request values
     * @return The encapsulated response
     */
    fun tokenGrantRefresh(requestVars: Map<String, String>): AuthSuccess {
        // Retrieve the refresh token
        val refreshToken: String? = requestVars["refresh_token"]

        // Check if the refresh token isn't null
        if (refreshToken != null) {
            // Find the refresh token in the database
            val refreshTokenObj: RefreshToken? = connRefresh.findByRefreshToken(refreshToken)

            if (refreshTokenObj != null) {
                // Generate a network petition to find the user information
                val objectMapper = ObjectMapper()
                val jsonRequest: JsonParser = objectMapper.createParser("{ \"id\": " + refreshTokenObj.userId + " }")
                val user: JsonNode? = RpcUtils.sendMessage("users.login", objectMapper.readTree(jsonRequest))

                // If the user information isn't null, generate a new access token
                if (user != null) {
                    // Determinate the actual time and the expiration time
                    val actualTime = System.currentTimeMillis()
                    val expirationTime = System.currentTimeMillis() + (expirationTimeNumber.toLong() * 60)

                    // Get the access token
                    val accessToken: String =
                        Jwts.builder().setId(user["id"].asText()).setSubject(user["email"].asText())
                            .setExpiration(Date(expirationTime)).setIssuedAt(Date(actualTime))
                            .signWith(signingKey, SignatureAlgorithm.HS512).compact()

                    // return the AuthSuccess object to the http call
                    return AuthSuccess(accessToken, "bearer", expirationTimeNumber.toLong(), refreshToken)
                } else {
                    // Return no validate operation if no find the user
                    throw LoginException("No couldn't validated the user")
                }
            } else {
                // Return no validate operation if no find the user
                throw LoginException("No couldn't validated the user")
            }
        } else {
            // If the body is bad formed, return malformed url exception
            throw MalformedURLException("The request body is malformed")
        }
    }

    /**
     * Method for register users in the database
     *
     * @param body request json body
     */
    fun registerUser(body: String) {
        // Instance the object mapper
        val objectMapper = ObjectMapper()

        // Parse the body
        val jsonNode: JsonNode = objectMapper.readTree(objectMapper.createParser(body))

        // Remove the password field from petition
        val password : String = jsonNode["password"].asText()
        (jsonNode as ObjectNode).remove("password")

        // Send the RPC Message to Users Server
        val jsonResult: JsonNode = RpcUtils.sendMessage("users.register", jsonNode) ?: throw DataException("Error registering the User information into database", null)

        // Insert the credentials into the database
        connCredential.insert(
            Credential(
                null,
                jsonResult["id"].asLong(),
                jsonResult["email"].asText(),
                password
            )
        )

        // Send a mail warning the register is successful
        sendEmail(
            jsonNode["email"].asText(),
            "Registry successfully",
            "Registry successfully",
            "The register of ${jsonNode["username"]} is complete"
        )
    }

    /**
     * Method for logout the user using the request token
     *
     * @param refreshTokenStr The user refresh token
     */
    fun logoutUser(refreshTokenStr: String) {
        connRefresh.findByRefreshToken(refreshTokenStr)?.let {
            connRefresh.remove(it)
        }
    }

    /**
     * Method for remove the user credentials
     *
     * @param credential The credential object
     * @return The deleted credential object
     */
    fun removeCredentials(credential: Credential): Credential {
        // Find all refresh tokens and remove it from the database
        connRefresh.findByUserId(credential.userId)?.forEach {
            connRefresh.remove(it)
        }

        // Find the user credentials and remove it from the database
        connCredential.findByUserId(credential.userId)?.let {
            connCredential.remove(it)
        }

        // Return the deleted credential
        return credential
    }
}