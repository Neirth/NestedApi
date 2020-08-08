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

import com.rabbitmq.client.Channel;

import java.io.StringReader;
import java.util.NoSuchElementException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;

// Internal packages of the project.
import io.neirth.nestedapi.Authentication.ServiceUtils;
import io.neirth.nestedapi.Authentication.Connectors.Connections;
import io.neirth.nestedapi.Authentication.Connectors.TokensConn;
import io.neirth.nestedapi.Authentication.Schemas.UserObj;

@Path("/auth")
public class AuthRest {
    @POST
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authToken(@Context final HttpServletRequest req, String body) {
        return ServiceUtils.processRequest(req, null, body, () -> {
            // TODO: Check if refresh token is present.

            /*
             * 0. TODO: Create endpoint for serve a authentication html page with jsp.
             * 
             * 1. First of all, check the type of validation is used, or check if the refresh token is present in Bearer Authentication Header
             * 
             * 2. Validate the authorization code.
             * 
             * 3. Validate the client in the case of this.
             * 
             * 4. Read the permited scopes.
             * 
             * 5. Generate Refresh token.
             * 
             * 6. Generate Access token.
             * 
             * 7. Return it to the client.
             */
            // Get authentication header.
            String authHeader = req.getHeader("Authentication");

            // Check if is present the refresh token
            if (authHeader.startsWith("Bearer")) {
                // TODO: Check the refresh token.
                TokensConn conn = null;

                try {
                    // Recovers the Token connection.
                    conn = Connections.getInstance().acquireAuths();

                    // Check if exist the token.
                    conn.read(authHeader.substring(7));
                } finally {
                    Connections.getInstance().releaseAuths(conn);
                }

            } else {
                // TODO: Check the type of validation.
            }

            return null;
        });
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
    public Response registerUser(@Context final HttpServletRequest req, String jsonRequest) {
        return ServiceUtils.processRequest(req, null, jsonRequest, () -> {
            // Prepare the bad request message.
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);

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

                // If the process is complete successfully, write a ok response.
                response = Response.ok(MediaType.APPLICATION_JSON);
            }

            return response;
        });
    }

    /**
     * Method for log out the user.
     * 
     * @param req Http headers
     * @return The response.
     */
    @POST
    @Path("/logout")
    public Response logoutUser(@Context final HttpServletRequest req) {
        return ServiceUtils.processRequest(req, null, null, () -> {
            // Prepare the response.
            ResponseBuilder response = null;

            // Acquire a Tokens Connection instance.
            Channel channel = Connections.getInstance().acquireBroker();

            // Prepare connections variable.
            TokensConn conn = null;

            try {
                // Get connection instance.
                conn = Connections.getInstance().acquireAuths();

                // Remove the token from database.
                conn.delete(req.getHeader("Authentication").substring(7));

                // Write the ok response.
                response = Response.status(Status.ACCEPTED);
            } catch (NoSuchElementException e) {
                // If the user was not found, write a not found response.
                response = Response.status(Status.NOT_FOUND);
            } finally {
                // Release the tokens connection instance.
                Connections.getInstance().releaseBroker(channel);
            }

            // Return the response.
            return response;
        });
    }
}