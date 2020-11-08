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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.neirth.nestedapi.Authentication.Controllers;

// Used libraries from Java Enterprise.
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.HttpHeaders;
import org.jboss.resteasy.spi.HttpRequest;
import com.rabbitmq.client.Channel;

// Packages for manage exception.
import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;

// Packages for manage data structures
import java.sql.Date;
import java.io.StringReader;
import java.util.Map;
import java.util.UUID;

// Packages for process Jwts and Json.
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

// Internal packages of the project.
import io.neirth.nestedapi.Authentication.ServiceUtils;
import io.neirth.nestedapi.Authentication.Connectors.Connections;
import io.neirth.nestedapi.Authentication.Connectors.TokensConn;
import io.neirth.nestedapi.Authentication.Schemas.UserObj;
import io.neirth.nestedapi.Authentication.Templates.Token;

@Path("/auth")
public class AuthRest {
    private final Long expirationTime = Long.valueOf(System.getenv("EXPIRATION_TIME"));

    /**
     * Http method for authenticate the users.
     * 
     * @param req         Http headers.
     * @param body        The www encoded variables.
     * @return The operation response.
     */
    @POST
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authToken(@Context final HttpRequest req, String body) {
        return ServiceUtils.processRequest(req, null, body, () -> {
            // Prepare the bad request message.
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);

            // Parse the body variables.
            Map<String, String> requestVars = ServiceUtils.parseFormEncoded(body);
            String grantType = requestVars.get("grant_type");

            // Determinate the type of authentication.
            switch (grantType) {
                case "password":
                    response = grantTypePassword(requestVars);
                    break;
                case "refresh_token":
                    response = grantTypeRefreshToken(requestVars);
                    break;
                default:
                    break;
            }

            return response;
        });
    }

    /**
     * Method for process the password petition.
     * 
     * @param requestVars Map with www encoded variables.
     * @return The operation response.
     */
    private ResponseBuilder grantTypePassword(Map<String, String> requestVars) throws InterruptedException, IOException, SQLException {
        // Prepare the bad request message.
        ResponseBuilder response = Response.status(Status.BAD_REQUEST);

        // Return the username and password.
        String username = requestVars.get("username"), password = requestVars.get("password");

        // Check if this isn't null.
        if (username != null || password != null) {
            // Recover the connection.
            TokensConn conn = Connections.getInstance().acquireAuths();

            try {
                // Recover from users microservice their data.
                UserObj user = RpcRequest.readUser(username);

                // Check if the password match.
                if (password.equals(user.getPassword().toString())) {
                    // Prepares the Json Response.
                    JsonObjectBuilder jsonResponse = Json.createObjectBuilder();

                    // Prepares the refresh and auth token.
                    String refreshToken = UUID.randomUUID().toString(), authToken;

                    // Instances the actual operation time.
                    long actualDate = System.currentTimeMillis();
                    long expirationDate = actualDate + this.expirationTime;

                    // Build the refresh token object.
                    Token token = new Token.Builder().setUserId(user.getId()).setToken(refreshToken)
                                                     .setValidFrom(new Date(actualDate)).build();

                    // Insert them into the database.
                    conn.create(token);

                    // Build the authentication token object.
                    authToken = Jwts.builder().setId(Long.toString(user.getId())).setSubject(user.getEmail().toString())
                                              .setIssuedAt(new Date(actualDate)).setExpiration(new Date(expirationDate))
                                              .signWith(ServiceUtils.getKey(), SignatureAlgorithm.HS512).compact();

                    // Build json response with access and refresh token.
                    jsonResponse.add("access_token", authToken);
                    jsonResponse.add("token_type", "Bearer");
                    jsonResponse.add("expires_in", this.expirationTime);
                    jsonResponse.add("refresh_token", refreshToken);

                    // TODO: Send email warning the login.

                    // Sends the response.
                    response = Response.status(Status.OK).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
                } else {
                   throw new NoSuchElementException();
                }
            } catch (NoSuchElementException e) {
                // Build the json error response.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                jsonResponse.add("error", "access_denied");
                jsonResponse.add("error_description", "Login information could not be validated correctly.");

                // If don't match, return a forbidden response.
                response = Response.status(Status.FORBIDDEN).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
            } finally {
                // Release the token connection.
                Connections.getInstance().releaseAuths(conn);
            }
        }

        return response;
    }

    /**
     * Method for process the refresh token petition.
     * 
     * @param requestVars Map with www encoded variables.
     * @return The operation response.
     */
    private ResponseBuilder grantTypeRefreshToken(Map<String, String> requestVars) throws InterruptedException, IOException, SQLException {
        // Prepare the bad request message.
        ResponseBuilder response = Response.status(Status.BAD_REQUEST);

        // Recover the connection.
        TokensConn conn = Connections.getInstance().acquireAuths();

        try {
            if (response != null) {
                // Prepares the Json Response.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();

                // Prepares the refresh and auth token.
                String refreshToken = requestVars.get("refresh_token"), authToken;

                // Recover the data from the databases.
                Token token = conn.read(refreshToken);
                UserObj user = RpcRequest.readUser(token.getUserId());

                // Instances the actual operation time.
                long actualDate = System.currentTimeMillis();
                long expirationDate = System.currentTimeMillis() + this.expirationTime;

                // Build the authentication token object.
                authToken = Jwts.builder().setId(Long.toString(user.getId())).setSubject(user.getEmail().toString())
                                          .setIssuedAt(new Date(actualDate)).setExpiration(new Date(expirationDate))
                                          .signWith(ServiceUtils.getKey(), SignatureAlgorithm.HS512).compact();
        
                // Build json response with access and refresh token.
                jsonResponse.add("access_token", authToken);
                jsonResponse.add("token_type", "Bearer");
                jsonResponse.add("expires_in", this.expirationTime);
                jsonResponse.add("refresh_token", refreshToken);

                // Sends the response.
                response = Response.status(Status.OK).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
            } else {
                throw new NoSuchElementException();
            }
        } catch (NoSuchElementException e) {
            // Build the json error response.
            JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
            jsonResponse.add("error", "access_denied");
            jsonResponse.add("error_description", "The token could not be properly validated.");

            // If don't match, return a forbidden response.
            response = Response.status(Status.FORBIDDEN).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
        } finally {
            // Release the token connection.
            Connections.getInstance().releaseAuths(conn);
        }

        return response;
    }

    /**
     * Method for register a user.
     * 
     * @param req         Http headers.
     * @param jsonRequest The request body.
     * @return The operation response.
     */
    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(@Context final HttpRequest req, String jsonRequest) {
        return ServiceUtils.processRequest(req, null, jsonRequest, () -> {
            // Prepare the bad request message.
            ResponseBuilder response;

            // If the body message is empty, don't process the petition.
            if (jsonRequest.length() != 0) {
                // Initialize the json object builder.
                final JsonObject jsonData = Json.createReader(new StringReader(jsonRequest)).readObject();

                // Updates only the columns with new data.
                UserObj user = new UserObj();

                // Set values into user rpc object.
                user.setName(jsonData.getString("name"));
                user.setSurname(jsonData.getString("surname"));
                user.setEmail(jsonData.getString("email"));
                user.setPassword(jsonData.getString("password"));
                user.setTelephone(jsonData.getString("telephone"));
                user.setBirthday(jsonData.getString("birthday"));
                user.setCountry(jsonData.getString("country"));
                user.setAddress(jsonData.getString("address"));
                user.setAddressInformation(jsonData.getString("addressInformation"));

                // Try to update the user from the database.
                RpcRequest.createUser(user);

                // TODO: Send email with verification.
                ServiceUtils.sendMail((String) user.getEmail(), "Welcome to NESTEDAPI", "Welcome to NESTEDAPI," + user.getName());

                // If the process is complete successfully, write a ok response.
                response = Response.status(Status.OK);
            } else {
                // Build the json error response.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                jsonResponse.add("error", "invalid_request");
                jsonResponse.add("error_description", "The information required for registration was not found.");

                // If the user was not found, write a not found response.
                response = Response.status(Status.BAD_REQUEST).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
            }

            return response;
        });
    }

    /**
     * Method for log out the user.
     * 
     * @param req         Http headers.
     * @return The operation response.
     */
    @POST
    @Path("/logout")
    public Response logoutUser(@Context final HttpRequest req) {
        return ServiceUtils.processRequest(req, null, null, () -> {
            // Prepare the response.
            ResponseBuilder response;

            // Acquire a Tokens Connection instance.
            Channel channel = Connections.getInstance().acquireBroker();

            // Prepare connections variable.
            TokensConn conn;

            try {
                // Get the token.
                String token = req.getHttpHeaders().getHeaderString(HttpHeaders.AUTHORIZATION);

                if (token != null) {
                    // Get connection instance.
                    conn = Connections.getInstance().acquireAuths();
    
                    // Remove the token from database.
                    conn.delete(token.substring(7));
    
                    // Write the ok response.
                    response = Response.status(Status.OK);
                } else {
                    throw new NoSuchElementException();
                }
            } catch (NoSuchElementException e) {
                // Build the json error response.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                jsonResponse.add("error", "access_denied");
                jsonResponse.add("error_description", "The token could not be properly validated.");

                // If the user was not found, write a not found response.
                response = Response.status(Status.FORBIDDEN).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
            } finally {
                // Release the tokens connection instance.
                Connections.getInstance().releaseBroker(channel);
            }

            // Return the response.
            return response;
        });
    }
}