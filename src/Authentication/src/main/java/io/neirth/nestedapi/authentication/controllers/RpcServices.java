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
package io.neirth.nestedapi.authentication.controllers;

// Used libraries from Java Standard.
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

// Used libraries from Java Enterprise.
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response.Status;

// Used libraries for AMQP operations.
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

// Used libraries for introspect the JWT token.
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

// Internal packages of the project.
import io.neirth.nestedapi.authentication.connectors.Connections;
import io.neirth.nestedapi.authentication.connectors.TokensConn;
import io.neirth.nestedapi.schemas.Request;
import io.neirth.nestedapi.schemas.Response;
import io.neirth.nestedapi.authentication.templates.Token;
import io.neirth.nestedapi.authentication.ServiceUtils;

public class RpcServices {
    /**
     * Method to receive AMQP traffic and route the correct method.
     * 
     * This method, called after the callback of the reception of a message, will
     * process all the information that has been passed by parameters to know what
     * method the request corresponds to, likewise, after the response is processed,
     * the client will be returned a operation status message.
     * 
     * @param channel  The channel where receives the petition.
     * @param delivery The petition.
     * @throws IOException
     */
    public void routeDelivery(Channel channel, Delivery delivery) {
        try {
            // Obtain a called method.
            String type = delivery.getProperties().getHeaders().get("x-remote-method").toString();

            // Read the request.
            Request request = Request.fromByteBuffer(ByteBuffer.wrap(delivery.getBody()));

            // Prepare the response.
            Response response = null;

            // Depending of the method called, we route the petition into the real method.
            switch (type) {
                case "IsValidToken":
                    response = isValidToken(request);
                    break;
                case "RemoveToken":
                    response = removeToken(request);
                    break;
                default:
                    break;
            }

            if (response != null) {
                // Prepare the properties of the response.
                BasicProperties replyProps = new BasicProperties.Builder().correlationId(delivery.getProperties().getCorrelationId()).build();

                // Publish the response into the private queue and sets the acknowledge.
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.toByteBuffer().array());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } else {
                throw new InternalServerErrorException("The response can't be processed.");
            }
        } catch (Exception e) {
            // In the case of crash, print the stack trace.
            e.printStackTrace();
        }
    }

    /**
     * RPC Method for check if the token is valid or not.
     * 
     * This method is used by the RPC interface to check the validity if the
     * authentication token by reading the status of the refresh token, as well as
     * whether it has been altered in any way validating the security component of
     * the token.
     * 
     * @param request The RPC request.
     * @return The RPC response.
     */
    public Response isValidToken(Request request) {
        // Prepare conn and response variables.
        TokensConn conn = null;
        Response response = new Response();

        try {
            // Acquire the token connection.
            conn = Connections.getInstance().acquireAuths();

            // Obtain the User ID and Refresh Token.
            Claims claims = Jwts.parser()
                                .setSigningKey(ServiceUtils.getKey())
                                .parseClaimsJws(request.getToken().toString())
                                .getBody();

            // Recover the refresh token encapsulated information.
            Token token = conn.read(claims.getIssuer());

            // Check the User ID from the Token with the User ID from the database.
            response.setStatus(Status.ACCEPTED.getStatusCode());
            response.setData(token.getUserId().equals(Long.valueOf(claims.getSubject())));
        } catch (Exception e) {
            // If occurs any exception, the token is not valid.
            response.setStatus(Status.FORBIDDEN.getStatusCode());
            response.setData(false);
        } finally {
            // Release the Token Connection.
            Connections.getInstance().releaseAuths(conn);
        }

        // Return the RPC Response.
        return response;
    }

    public Response removeToken(Request request) {
        // Prepare conn and response variables.
        TokensConn conn = null;
        Response response = new Response();

        try {
            // Acquire the token connection.
            conn = Connections.getInstance().acquireAuths();

            // Remove the refresh token from database.
            conn.delete(request.getToken().toString());

            // Check the User ID from the Token with the User ID from the database.
            response.setStatus(Status.ACCEPTED.getStatusCode());
        } catch (NoSuchElementException e) {
            // If the token doesn't exist in the database, return 404.
            response.setStatus(Status.NOT_FOUND.getStatusCode());
        } catch (Exception e) {
            // If occurs any exception, return server error.
            response.setStatus(Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } finally {
            // Release the Token Connection.
            Connections.getInstance().releaseAuths(conn);
        }

        // Return the RPC Response.
        return response;
    }
}