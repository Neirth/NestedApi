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
package io.neirth.nestedapi.Users;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
// Used libraries from Java Enterprise.
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.jboss.resteasy.spi.HttpRequest;

// Used library for logging the server events.
import org.jboss.logging.Logger;

// Used libraries for process the jwt token.
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

// Internal packages of the project.
import io.neirth.nestedapi.Users.Controllers.RpcRequest;

public class ServiceUtils {
    // The instance of logger.
    private static final Logger loggerSystem = Logger.getLogger(ServiceApp.class);

    // The callback with custom procedures.
    public interface RestCallback {
        ResponseBuilder run(String token, Claims tokenData) throws Exception;
    }

    /**
     * Method that process the request received from REST API and generate a
     * response in the json format.
     * 
     * This method, with the corresponding RestCallback to do the specific tasks,
     * retrieves the information from the body and executes the callback lambda, the
     * message returned is a HTTP response with Json Body.
     * 
     * @param req         The headers of http request.
     * @param paramId     The paramId of the requested object.
     * @param body The body of http request.
     * @param callback    The lambda callback
     * @return The response object.
     */
    public static Response processRequest(HttpRequest req, Long paramId, String body, RestCallback callback) {
        // Initialize the response builder.
        ResponseBuilder response;

        try {
            // Run the callback and get the response.
            response = callback.run(null, null);
        } catch (Exception e) {
            // Write the exception in the log.
            writeServerException(e);

            // Build the json error response.
            JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
            jsonResponse.add("error", "server_error");
            jsonResponse.add("error_description", "An error has occurred on the server while processing your request.");

            // Create error response.
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
        }

        // Throws the response.
        return response.build();
    }

    /**
     * Method that process the request received from REST API and generate a
     * response in the json format.
     * 
     * This method, with the corresponding RestCallback to do the specific tasks,
     * retrieves the information from the body and executes the callback lambda, the
     * message returned is a HTTP response with Json Body.
     * 
     * @param req         The headers of http request.
     * @param paramId     The paramId of the requested object.
     * @param body The body of http request.
     * @param callback    The lambda callback
     * @return The response object.
     */
    public static Response processUserRequest(HttpRequest req, Long paramId, Object body, RestCallback callback) {
        // Initialize the response builder.
        ResponseBuilder response;

        // Obtains a token string and her data.
        String authHeader = req.getHttpHeaders().getHeaderString(HttpHeaders.AUTHORIZATION);
        String token = (authHeader != null) ? authHeader.substring(7).trim() : null;

        try {
            // Validate the token.
            if (RpcRequest.isValidToken(token)) {
                // Parse the token data
                Claims tokenData = Jwts.parser().parseClaimsJws(token).getBody();

                // Run the callback and get the response.
                response = callback.run(token, tokenData);
            } else {
                // Build the json error response.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
                jsonResponse.add("error", "access_denied");
                jsonResponse.add("error_description", "Login information could not be validated correctly.");

                // If the user was not found, write a not found response.
                response = Response.status(Status.FORBIDDEN).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
            }
        } catch (Exception e) {
            // Write the exception in the log.
            writeServerException(e);

            // Build the json error response.
            JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
            jsonResponse.add("error", "server_error");
            jsonResponse.add("error_description", "An error has occurred on the server while processing your request.");

            // Create error response.
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
        }

        // Throws the response.
        return response.build();
    }

    /**
     * Method to write the exception in the logs.
     * 
     * It's useful for write the stack trace in debug mode or only the error in
     * production.
     * 
     * @param e The exception caught.
     */
    public static void writeServerException(Exception e) {
        if (ServiceUtils.getLoggerSystem().isDebugEnabled()) {
            // If the log level is set to debug, write the trace stack.
            ServiceUtils.getLoggerSystem().debug("An exception has occurred, getting the stacktrace of the exception: ");
            e.printStackTrace();
        } else {
            // Information for production log.
            ServiceUtils.getLoggerSystem().error("An exception has occurred, " + e.toString());
        }
    }

    /**
     * Method to access the event log of this module.
     * 
     * @return The logger instance.
     */
    public static Logger getLoggerSystem() {
        return loggerSystem;
    }
}