/*
 * MIT License
 *
 * Copyright (c) 2020 NestedApi Project
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
package io.neirth.nestedapi.Authentication.Controllers;

// Used libraries from Java Standard.
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.NoSuchElementException;

// Used libraries from Java Enterprise.
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.servlet.http.HttpServletRequest;

// Used libraries for JWT Processing.
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

// Internal packages of the project.
import io.neirth.nestedapi.Authentication.ServiceUtils;
import io.neirth.nestedapi.Authentication.Connectors.Connections;
import io.neirth.nestedapi.Authentication.Connectors.TokensConn;

@Path("/auth")
public class AuthRest {
    @POST
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authToken(@Context final HttpServletRequest req, Map<String, String> body) {
        return ServiceUtils.processRequest(req, null, body, () -> {
            // TODO: Check if refresh token is present.

            // Convert Object to Map.
            Map<String, String> bodyMap = (Map<String, String>) body;

            return null;
        });
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(@Context final HttpServletRequest req, String body) {
        return ServiceUtils.processRequest(req, null, body, () -> {
            String jsonRequest = (String) body;

            return null;
        });
    }

    @POST
    @Path("/logout")
    public Response logoutUser(@Context final HttpServletRequest req) {
        return ServiceUtils.processRequest(req, null, null, () -> {
            // Prepare the response.
            ResponseBuilder response = null;

            // Acquire a Tokens Connection instance.
            TokensConn conn = Connections.getInstance().acquireAuths();

            try {
                // Recovers all http headers.
                List<String> authHeader = Collections.list(req.getHeaders(HttpHeaders.AUTHORIZATION));

                // Obtains a token string and her data.
                String token = (authHeader.size() != 0) ? authHeader.get(0).substring(7).trim() : null;

                // Parse the token data into a claims list.
                Claims claims = Jwts.parser()
                                    .setSigningKey(ServiceUtils.getKey())
                                    .parseClaimsJws(token)
                                    .getBody();

                // Delete the token in the database.
                conn.delete(claims.getIssuer());

                // Write the ok response.
                response = Response.status(Status.ACCEPTED);
            } catch (NoSuchElementException e) {
                // If the token was not found, write a not found response.
                response = Response.status(Status.NOT_FOUND);
            } catch (ExpiredJwtException e) {
                // If the token was expired, write a forbidden response.
                response = Response.status(Status.FORBIDDEN);
            } catch (SecurityException e) {
                // If the token has security problems, write a forbidden response.
                response = Response.status(Status.FORBIDDEN);
            } finally {
                // Release the tokens connection instance.
                Connections.getInstance().releaseAuths(conn);
            }

            // Return the response.
            return response;
        });
    }
}