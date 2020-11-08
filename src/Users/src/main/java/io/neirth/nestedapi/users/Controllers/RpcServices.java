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

// Used libraries from Java Standard.
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;

// Used libraries from Java Enterprise.
import javax.ws.rs.InternalServerErrorException;
import javax.xml.bind.DatatypeConverter;
import javax.ws.rs.core.Response.Status;

// Used libraries for AMQP operations.
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

// Internal packages of the project.
import io.neirth.nestedapi.users.connectors.Connections;
import io.neirth.nestedapi.users.connectors.UsersConn;
import io.neirth.nestedapi.schemas.Request;
import io.neirth.nestedapi.schemas.Response;
import io.neirth.nestedapi.schemas.UserObj;
import io.neirth.nestedapi.users.templates.Country;
import io.neirth.nestedapi.users.templates.User;

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
                case "CreateUser":
                    response = createUser(request);
                    break;
                case "ReadUser":
                    response = readUser(request);
                    break;
                case "UpdateUser":
                    response = updateUser(request);
                    break;
                case "DeleteUser":
                    response = deleteUser(request);
                    break;
            }

            if (response != null) {
                // Prepare the properties of the response.
                BasicProperties replyProps = new BasicProperties.Builder().correlationId(delivery.getProperties().getCorrelationId()).build();

                // Publish the response into the private queue and sets the acknowledge.
                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.toByteBuffer().array());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } else {
                throw new InternalServerErrorException("The request is no processed correctly");
            }
        } catch (Exception e) {
            // In the case of crash, print the stack trace.
            e.printStackTrace();
        }
    }

    /**
     * This method is used for delete a user throughout RPC channel.
     * 
     * In some cases, the modules need delete a user, which maybe is banned from the
     * service or another thing.
     * 
     * @param request The RPC petition.
     * @return The response message.
     */
    public Response deleteUser(Request request) {
        // Prepare the conn and response variable
        UsersConn conn = null;
        Response response = new Response();

        try {
            // Prepare the database connection.
            conn = Connections.getInstance().acquireUsers();

            // Create a dummy user object with the id only.
            User user = new User.Builder(request.getId()).build();

            // Try to delete the user from the database.
            conn.delete(user);

            // Set the ok status code
            response.put("status", Status.OK.getStatusCode());
        } catch (NoSuchElementException e) {
            response.put("status", Status.NOT_FOUND.getStatusCode());
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("status", Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.put("message", e.getMessage());
        } finally {
            // Release the connection with the database.
            Connections.getInstance().releaseUsers(conn);
        }

        // Return the response.
        return response;
    }

    /**
     * This method is used for update a user throughout RPC channel.
     * 
     * Some modules needs update the user information, such as specific job module
     * which works with user module.
     * 
     * @param request The RPC petition.
     * @return The response message.
     */
    public Response updateUser(Request request) {
        // Prepare the conn and response variable
        UsersConn conn = null;
        Response response = new Response();

        try {
            // Prepare the database connection.
            conn = Connections.getInstance().acquireUsers();

            // Read the old user.
            User auxUser = conn.read(request.getUser().getId());

            // Create a dummy user object with the id only.
            User user = new User.Builder(request.getUser().getId())
                    .setName(request.getUser().getName() != null ? request.getUser().getName().toString() : auxUser.getName())
                    .setSurname(request.getUser().getSurname() != null ? request.getUser().getSurname().toString() : auxUser.getSurname())
                    .setEmail(request.getUser().getEmail() != null ? request.getUser().getEmail().toString() : auxUser.getEmail())
                    .setPassword(request.getUser().getPassword() != null ? request.getUser().getPassword().toString() : auxUser.getPassword())
                    .setTelephone(request.getUser().getTelephone() != null ? request.getUser().getTelephone().toString() : auxUser.getTelephone())
                    .setBirthday(request.getUser().getBirthday() != null ? DatatypeConverter.parseDateTime(request.getUser().getBirthday().toString()).getTime() : auxUser.getBirthday())
                    .setCountry(request.getUser().getCountry() != null ? Enum.valueOf(Country.class, request.getUser().getCountry().toString()) : auxUser.getCountry())
                    .setAddress(request.getUser().getAddress() != null ? request.getUser().getAddress().toString() : auxUser.getAddress())
                    .setAddressInformation(request.getUser().getAddressInformation() != null ? request.getUser().getAddressInformation().toString() : auxUser.getAddressInformation())
                    .build();

            // Update the user.
            conn.update(user);

            // Set the ok status code
            response.put("status", Status.OK.getStatusCode());
        } catch (NoSuchElementException e) {
            response.put("status", Status.NOT_FOUND.getStatusCode());
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("status", Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.put("message", e.getMessage());
        } finally {
            // Release the connection with the database.
            Connections.getInstance().releaseUsers(conn);
        }

        // Return the response.
        return response;
    }

    /**
     * This method is used for read a user throughout RPC channel.
     * 
     * When user is trying to login, other service module will try to read the user
     * information, creating a RPC message to this server.
     * 
     * @param request The RPC petition.
     * @return The response message.
     */
    public Response readUser(Request request) {
        // Prepare the conn and response variable
        UsersConn conn = null;
        Response response = new Response();

        try {
            // Prepare the database connection.
            conn = Connections.getInstance().acquireUsers();

            // Create a dummy user object with the id only.
            UserObj userObj = new UserObj();
            User user = (request.getEmail() != null) ? conn.readFromEmail(request.getEmail().toString()) : conn.read(request.getId());

            // Add user information into RPC message.
            userObj.setId(user.getId());
            userObj.setName(user.getName());
            userObj.setSurname(user.getSurname());
            userObj.setEmail(user.getEmail());
            userObj.setPassword(user.getPassword());
            userObj.setTelephone(user.getTelephone());
            userObj.setBirthday(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(user.getBirthday()));
            userObj.setCountry(user.getCountry().getCountryName());
            userObj.setAddress(user.getAddress());
            userObj.setAddressInformation(user.getAddressInformation());

            // Set the ok status code
            response.put("status", Status.OK.getStatusCode());
            response.put("object", userObj);
        } catch (NoSuchElementException e) {
            response.put("status", Status.NOT_FOUND.getStatusCode());
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("status", Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.put("message", e.getMessage());
        } finally {
            // Release the connection with the database.
            Connections.getInstance().releaseUsers(conn);
        }

        // Return the response.
        return response;
    }

    /**
     * This method processes the creation of a user through the RPC channel.
     * 
     * Normally this situation can occur when a user is registering in our service,
     * therefore this method will be important for our operations.
     * 
     * @param request The RPC petition.
     * @return The response message.
     */
    public Response createUser(Request request) {
        // Prepare the conn and response variable
        UsersConn conn = null;
        Response response = new Response();

        try {
            // Prepare the database connection.
            conn = Connections.getInstance().acquireUsers();

            // Create a user handled by RPC message.
            User user = new User.Builder(request.getUser().getId()).setName(request.getUser().getName().toString())
                    .setSurname(request.getUser().getSurname().toString())
                    .setEmail(request.getUser().getEmail().toString())
                    .setPassword(request.getUser().getPassword().toString())
                    .setTelephone(request.getUser().getTelephone().toString())
                    .setBirthday(DatatypeConverter.parseDateTime(request.getUser().getBirthday().toString()).getTime())
                    .setCountry(Enum.valueOf(Country.class, request.getUser().getCountry().toString()))
                    .setAddress(request.getUser().getAddress().toString())
                    .setAddressInformation(request.getUser().getAddressInformation().toString()).build();

            // Set the user in the database.
            long id = conn.create(user);

            // Set the ok status code
            response.put("status", Status.OK.getStatusCode());
            response.put("object_id", id);
        } catch (NoSuchElementException e) {
            response.put("status", Status.NOT_FOUND.getStatusCode());
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("status", Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.put("message", e.getMessage());
        } finally {
            // Release the connection with the database.
            Connections.getInstance().releaseUsers(conn);
        }

        // Return the response.
        return response;
    }
}