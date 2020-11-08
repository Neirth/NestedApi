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
package io.neirth.nestedapi.users.controllers;

// Utilities used from Java Standard Framework
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;

// Used libraries for json serialize and deserialize.
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

// Used libraries for http operations
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;
import org.jboss.resteasy.spi.HttpRequest;

// Internal packages of the project.
import io.neirth.nestedapi.users.ServiceUtils;
import io.neirth.nestedapi.users.connectors.Connections;
import io.neirth.nestedapi.users.connectors.UsersConn;
import io.neirth.nestedapi.users.templates.Country;
import io.neirth.nestedapi.users.templates.User;

@Path("/users")
public class UsersRest {
    /**
     * Method to get a user with param passed in the path.
     * 
     * @param req     Request header
     * @return The user response.
     */
    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context final HttpRequest req) {
        return ServiceUtils.processUserRequest(req, null, null, (token, tokenData) -> {
            // Prepare the response
            ResponseBuilder response;

            // Try to acquire the connection.
            UsersConn conn = Connections.getInstance().acquireUsers();

            try {
                // Initialize the json object builder.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();

                // Try to read the user information.
                User user = conn.read(Long.parseLong(tokenData.getId()));

                // Add all information into the json object builder
                jsonResponse.add("id", user.getId());
                jsonResponse.add("name", user.getName());
                jsonResponse.add("surname", user.getSurname());
                jsonResponse.add("email", user.getEmail());
                jsonResponse.add("password", user.getPassword());
                jsonResponse.add("telephone", user.getTelephone());
                jsonResponse.add("birthday", (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")).format(user.getBirthday()));
                jsonResponse.add("country", user.getCountry().name());

                // Insert correctly the nullable information
                if (user.getAddress() != null) {
                    jsonResponse.add("address", user.getAddress());
                } else {
                    jsonResponse.addNull("address");
                }

                // Insert correctly the nullable information
                if (user.getAddressInformation() != null) {
                    jsonResponse.add("addressInformation", user.getAddressInformation());
                } else {
                    jsonResponse.addNull("addressInformation");
                }

                // If the process is complete successfully, write a ok response.
                response = Response.status(Status.OK).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
            } catch (NoSuchElementException e) {
                // Build the json error response.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                jsonResponse.add("error", "access_denied");
                jsonResponse.add("error_description", "Login information could not be validated correctly.");

                // If the user was not found, write a not found response.
                response = Response.status(Status.FORBIDDEN).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
            } finally {
                // Return the users connection.
                Connections.getInstance().releaseUsers(conn);
            }

            // Return the message.
            return response;
        });
    }

    /**
     * Method to update a user with param passed in the path.
     * 
     * @param req         Request header
     * @param jsonRequest The request body.
     * @return The user response.
     */
    @PUT
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response modify(@Context final HttpRequest req, String jsonRequest) {
        return ServiceUtils.processUserRequest(req, null, jsonRequest, (token, tokenData) -> {
            // Prepare the bad request message.
            ResponseBuilder response = Response.status(Status.BAD_REQUEST);

            // If the body message is empty, don't process the petition.
            if (jsonRequest.length() != 0) {
                // Initialize the json object builder.
                final JsonObject jsonData = Json.createReader(new StringReader(jsonRequest)).readObject();

                // Try to read the user information.
                UsersConn conn = Connections.getInstance().acquireUsers();

                try {
                    // Get the old user.
                    User auxUser = conn.read(Long.parseLong(tokenData.getId()));

                    // Updates only the columns with new data.
                    User user = new User.Builder(Long.valueOf(tokenData.getId()))
                            .setName(jsonData.isNull("name") ? jsonData.getString("name") : auxUser.getName())
                            .setSurname(jsonData.isNull("surname") ? jsonData.getString("surname") : auxUser.getSurname())
                            .setEmail(jsonData.isNull("email") ? jsonData.getString("email") : auxUser.getEmail())
                            .setPassword(jsonData.isNull("password") ? jsonData.getString("password") : auxUser.getPassword())
                            .setTelephone(jsonData.isNull("telephone") ? jsonData.getString("telephone") : auxUser.getTelephone())
                            .setBirthday(jsonData.isNull("birthday") ? DatatypeConverter.parseDateTime(jsonData.getString("birthday")).getTime() : auxUser.getBirthday())
                            .setCountry(jsonData.isNull("country") ? Enum.valueOf(Country.class, jsonData.getString("country")) : auxUser.getCountry())
                            .setAddress(jsonData.isNull("address") ? jsonData.getString("address") : auxUser.getAddress())
                            .setAddressInformation(jsonData.isNull("addressInformation") ? jsonData.getString("addressInformation") : auxUser.getAddressInformation())
                            .build();

                    // Try to update the user from the database.
                    conn.update(user);

                    // If the process is complete successfully, write a ok response.
                    response = Response.status(Status.OK);
                } catch (NoSuchElementException e) {
                    // Build the json error response.
                    JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                    jsonResponse.add("error", "access_denied");
                    jsonResponse.add("error_description", "Login information could not be validated correctly.");

                    // If the user was not found, write a not found response.
                    response = Response.status(Status.FORBIDDEN).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
                } finally {
                    // Return the users connection.
                    Connections.getInstance().releaseUsers(conn);
                }
            }

            // Return the message.
            return response;
        });
    }

    /**
     * Method to delete a user with param passed in the path.
     * 
     * @param req     Request header.
     * @return The user response.
     */
    @DELETE
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@Context final HttpRequest req) {
        return ServiceUtils.processUserRequest(req, null, null, (token, tokenData) -> {
            // Prepare the response.
            ResponseBuilder response;

            // Try to acquire connection.
            UsersConn conn = Connections.getInstance().acquireUsers();

            try {
                // Try to get the requested user.
                User user = conn.read(Long.parseLong(tokenData.getId()));

                // Try to delete the requested user.
                conn.delete(user);

                // If the process is successfully, return the ok message.
                response = Response.status(Status.OK);
            } catch (NoSuchFieldError e) {
                // Build the json error response.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                jsonResponse.add("error", "access_denied");
                jsonResponse.add("error_description", "Login information could not be validated correctly.");

                // If the user was not found, write a not found response.
                response = Response.status(Status.FORBIDDEN).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
            } finally {
                // Release the connection.
                Connections.getInstance().releaseUsers(conn);
            }

            // Return the response.
            return response;
        });
    }
}