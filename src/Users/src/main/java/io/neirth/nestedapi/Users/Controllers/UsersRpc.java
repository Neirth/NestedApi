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
package io.neirth.nestedapi.Users.Controllers;

// Libraries used from Java Enterprise.
import javax.xml.bind.DatatypeConverter;

// Libraries used for AMQP operations.
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

// Internal packages of the project.
import io.neirth.nestedapi.Users.ServiceUtils;
import io.neirth.nestedapi.Users.Connectors.Connections;
import io.neirth.nestedapi.Users.Connectors.UsersConn;
import io.neirth.nestedapi.Users.Templates.Country;
import io.neirth.nestedapi.Users.Templates.User;

public class UsersRpc {

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
        // Obtain a called method.
        String type = (String) delivery.getProperties().getHeaders().get("x-remote-method");

        // Prepare the response.
        byte[] response = null;

        try {
            // Depending of the method called, we route the petition into the real method.
            switch (type) {
                case "CreateUser":
                    response = createUser(delivery);
                    break;
                case "ReadUser":
                    response = readUser(delivery);
                    break;
                case "UpdateUser":
                    response = updateUser(delivery);
                    break;
                case "DeleteUser":
                    response = deleteUser(delivery);
                    break;
            }

            // Prepare the properties of the response.
            BasicProperties replyProps = new BasicProperties.Builder()
                    .correlationId(delivery.getProperties().getCorrelationId()).build();

            // Publish the response into the private queue and sets the acknowledge.
            channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response);
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        } catch (Exception e) {
            // In the case of crash, print the stack trace.
            e.printStackTrace();
        }
    }

    /**
     * This method processes the creation of a user through the RPC channel.
     * 
     * Normally this situation can occur when a user is registering in our service,
     * therefore this method will be important for our operations.
     * 
     * @param delivery The RPC petition.
     * @return The response message.
     * @throws Exception If an exception occurs.
     */
    private byte[] createUser(Delivery delivery) throws Exception {
        return ServiceUtils.processMessage(delivery, "CreateUser", (consumedDatum) -> {
            // Prepare the database connection.
            UsersConn connection = Connections.getInstance().acquireUsers();

            try {
                // Create a user handled by RPC interface.
                User user = new User.Builder(null).setName((String) consumedDatum.get("name"))
                        .setSurname((String) consumedDatum.get("surname")).setEmail((String) consumedDatum.get("email"))
                        .setPassword((String) consumedDatum.get("password"))
                        .setTelephone((String) consumedDatum.get("telephone"))
                        .setBirthday(DatatypeConverter.parseDateTime((String) consumedDatum.get("birthday")).getTime())
                        .setCountry(Enum.valueOf(Country.class, (String) consumedDatum.get("country")))
                        .setAddress((String) consumedDatum.get("address"))
                        .setAddressInformation((String) consumedDatum.get("addressInformation")).build();

                // Set the user in the database.
                connection.create(user);
            } catch (Exception e) {
                // Print the stacktrace if crash.
                e.printStackTrace();
            } finally {
                // Release the connection with the database.
                Connections.getInstance().releaseUsers(connection);
            }

            // Return null because the operation no generates a response.
            return null;
        });
    }

    /**
     * This method is used for read a user throught RPC channel.
     * 
     * When user is trying to login, other service module will try to read the user
     * information, creating a RPC message to this server.
     * 
     * @param delivery The RPC petition.
     * @return The response message.
     * @throws Exception If an exception occurs.
     */
    private byte[] readUser(Delivery delivery) throws Exception {
        return ServiceUtils.processMessage(delivery, "ReadUser", (consumedDatum) -> {
            // Prepare the database connection.
            UsersConn connection = Connections.getInstance().acquireUsers();
            User user = null;

            try {
                // Try to recover the user from database.
                user = connection.read((long) consumedDatum.get("id"));
            } finally {
                // Release the connection with the database.
                Connections.getInstance().releaseUsers(connection);
            }

            return user;
        });
    }

    /**
     * This method is used for update a user throught RPC channel.
     * 
     * Some modules needs update the user information, such as specific job module
     * which works with user module.
     * 
     * @param delivery The RPC petition.
     * @return The response message.
     * @throws Exception If an exception occurs.
     */
    private byte[] updateUser(Delivery delivery) throws Exception {
        return ServiceUtils.processMessage(delivery, "UpdateUser", (consumedDatum) -> {
            // Prepare the database connection.
            UsersConn connection = Connections.getInstance().acquireUsers();

            try {
                // Get the old user.
                User auxUser = connection.read((long) consumedDatum.get("id"));

                // Updates only the columns with new data.
                User user = new User.Builder((long) consumedDatum.get("id"))
                        .setName(consumedDatum.get("name") != null 
                                ? (String) consumedDatum.get("name")
                                : auxUser.getName())
                        .setSurname(consumedDatum.get("surname") != null 
                                ? (String) consumedDatum.get("surname")
                                : auxUser.getSurname())
                        .setEmail(consumedDatum.get("email") != null 
                                ? (String) consumedDatum.get("email")
                                : auxUser.getEmail())
                        .setPassword(consumedDatum.get("password") != null 
                                ? (String) consumedDatum.get("password")
                                : auxUser.getPassword())
                        .setTelephone(consumedDatum.get("telephone") != null 
                                ? (String) consumedDatum.get("telephone")
                                : auxUser.getTelephone())
                        .setBirthday(consumedDatum.get("birthday") != null
                                ? DatatypeConverter.parseDateTime((String) consumedDatum.get("birthday")).getTime()
                                : auxUser.getBirthday())
                        .setCountry(consumedDatum.get("country") != null
                                ? Enum.valueOf(Country.class, (String) consumedDatum.get("country"))
                                : auxUser.getCountry())
                        .setAddress(consumedDatum.get("address") != null 
                                ? (String) consumedDatum.get("address")
                                : auxUser.getAddress())
                        .setAddressInformation(consumedDatum.get("addressInformation") != null
                                ? (String) consumedDatum.get("addressInformation")
                                : auxUser.getAddressInformation())
                        .build();

                // Try to update the user from the database.
                connection.update(user);
            } finally {
                // Release the connection with the database.
                Connections.getInstance().releaseUsers(connection);
            }

            // Return null because the operation no generates a response.
            return null;
        });
    }

    /**
     * This method is used for delete a user throught RPC channel.
     * 
     * In some cases, the modules need delete a user, which maybe is banned from the 
     * service or another thing.
     * 
     * @param delivery The RPC petition.
     * @return The response message.
     * @throws Exception If an exception occurs.
     */
    private byte[] deleteUser(Delivery delivery) throws Exception {
        return ServiceUtils.processMessage(delivery, "DeleteUser", (consumedDatum) -> {
            // Prepare the database connection.
            UsersConn connection = Connections.getInstance().acquireUsers();

            try {
                // Create a dummy user object with the id only.
                User user = new User.Builder((long) consumedDatum.get("id")).build();

                // Try to delete the user from the database.
                connection.delete(user);
            } finally {
                // Release the connection with the database.
                Connections.getInstance().releaseUsers(connection);
            }

            // Return null because the operation no generates a response.
            return null;
        });
    }

}