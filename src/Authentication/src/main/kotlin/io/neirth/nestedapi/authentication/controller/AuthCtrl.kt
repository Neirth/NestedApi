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
package io.neirth.nestedapi.authentication.controller

import io.neirth.nestedapi.authentication.domain.response.AuthSuccess
import io.neirth.nestedapi.authentication.domain.Credential
import io.neirth.nestedapi.authentication.service.AuthService
import io.neirth.nestedapi.authentication.util.*
import io.neirth.nestedapi.authentication.util.annotation.RpcMessage
import java.net.MalformedURLException
import javax.ws.rs.HeaderParam
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/auth")
class AuthCtrl(private val authService: AuthService) {
    /**
     * Http Method to process all related with the token and their lifecycle
     *
     * @param body The body content with www form encoded
     * @return The authentication successful
     */
    @POST
    @Path("token")
    fun authToken(body: String): AuthSuccess {
        // Get the map with the parameters
        val requestVars: Map<String, String> = parseFormEncoded(body)

        return when {
            /*
             * The case of grant type is password
             */
            requestVars["grant_type"] == "password" -> authService.tokenGrantPasswd(requestVars)

            /*
             * The case if is a access token renew
             */
            requestVars["grant_type"] == "refresh_token" -> authService.tokenGrantRefresh(requestVars)

            /*
             * Indicate the operation type isn't supported in this moment
             */
            else -> throw MalformedURLException("The grant_type option isn't supported")
        }
    }

    /**
     * Http Method for register the users in respective service
     *
     * @param body The body encoded JSON user
     */
    @POST
    @Path("register")
    fun registerUser(body: String) {
        authService.registerUser(body)
    }

    /**
     * Http Method for logout the users
     *
     * @param refreshTokenStr User refresh token
     */
    @POST
    @Path("logout")
    fun logoutUser(@HeaderParam("Authorization") refreshTokenStr: String) {
        authService.logoutUser(refreshTokenStr.substring(7))
    }

    /**
     * RPC Method for remove the users credentials
     *
     * @param credential The credential encapsulated
     * @return The deleted credential
     */
    @RpcMessage(topic = "auths", queue = "remove")
    fun deleteCredentials(credential: Credential): Credential {
        return authService.removeCredentials(credential)
    }
}
