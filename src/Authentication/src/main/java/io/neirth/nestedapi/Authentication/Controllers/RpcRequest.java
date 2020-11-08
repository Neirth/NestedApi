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

// Used libraries from Java Standard.
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

// Used libraries from Java Enterprise.
import javax.ws.rs.core.Response.Status;

// Used libraries for AMQP operations.
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

// Internal packages of the project.
import io.neirth.nestedapi.Authentication.Connectors.Connections;
import io.neirth.nestedapi.Authentication.Schemas.Request;
import io.neirth.nestedapi.Authentication.Schemas.Response;
import io.neirth.nestedapi.Authentication.Schemas.UserObj;

public class RpcRequest {
    /**
     * Remote method to create users in users endpoint.
     * 
     * When a user registers on our platform, their data must be saved in the
     * database. Since each endpoint has its own database and RPC interface, we will
     * launch the request via RPC to the user endpoint and wait for a response.
     * 
     * @param user The user object to send.
     * @throws IOException If the operation returns any exception that is not Okay, throw this exception.
     * @throws InterruptedException If the acquire process was interrupted, throws this exception.
     */
    public static void createUser(UserObj user) throws IOException, InterruptedException {
        // Prepare the variables.
        Channel channel = null;

        try {
            // Acquire the broker connection.
            channel = Connections.getInstance().acquireBroker();

            // Instance the properties.
            String corrId = UUID.randomUUID().toString();
            String replyTo = channel.queueDeclare().getQueue();

            // Add headers map.
            Map<String, Object> headers = new HashMap<>();
            headers.put("x-remote-method", "CreateUser");

            // Encapsulate into BasicProperties the instanced properties.
            BasicProperties props = new BasicProperties().builder().correlationId(corrId).replyTo(replyTo).headers(headers).build();

            // Build the request message.
            Request request = Request.newBuilder().setUser(user).build();

            // Send the request via RPC.
            channel.basicPublish("", "users", props, request.toByteBuffer().array());

    
            // Prepare the response variable.
            BlockingQueue<Response> response = new ArrayBlockingQueue<>(1);

            // Create a consumer the response.
            String cTag = channel.basicConsume(replyTo, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    response.offer(Response.fromByteBuffer(ByteBuffer.wrap(delivery.getBody())));
                }
            }, consumerTag -> {
            });

            // Wait the response.
            Response responseObj = response.take();

            // Throw a Exception if the request fails.
            if ((Integer) responseObj.getStatus() != Status.ACCEPTED.getStatusCode())
                throw new IOException(responseObj.getMessage().toString());

            // Cancel the consumer.
            channel.basicCancel(cTag);
        } finally {
            // Release the broker connection.
            Connections.getInstance().releaseBroker(channel);
        }
    }

    /**
     * Remote method for read users from users endpoint.
     * 
     * Since the Domain Driven Design architecture is being implemented in this
     * project. We will use remote methods to work with the other servers that have
     * been implemented in the project. Thus reducing the service is monolithic and
     * can be easily scalable.
     * 
     * @param email The user email.
     * @return The object of the response.
     * @throws IOException If the operation returns any exception that is not Okay, throw this exception.
     * @throws InterruptedException If the acquire process was interrupted, throws this exception.
     */
    public static UserObj readUser(String email) throws IOException, InterruptedException {
        // Prepare the variables.
        Channel channel = null;
        UserObj obj;

        try {
            // Acquire the broker connection.
            channel = Connections.getInstance().acquireBroker();

            // Instance the properties.
            String corrId = UUID.randomUUID().toString();
            String replyTo = channel.queueDeclare().getQueue();

            // Add headers map.
            Map<String, Object> headers = new HashMap<>();
            headers.put("x-remote-method", "ReadUser");

            // Encapsulate into BasicProperties the instanced properties.
            BasicProperties props = new BasicProperties().builder().correlationId(corrId).replyTo(replyTo).headers(headers).build();

            // Build the request message.
            Request request = Request.newBuilder().setEmail(email).build();

            // Send the request via RPC.
            channel.basicPublish("", "users", props, request.toByteBuffer().array());

            // Prepare the response variable.
            BlockingQueue<Response> response = new ArrayBlockingQueue<>(1);

            // Create a consumer the response.
            String cTag = channel.basicConsume(replyTo, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    response.offer(Response.fromByteBuffer(ByteBuffer.wrap(delivery.getBody())));
                }
            }, consumerTag -> {
            });

            // Wait the response.
            Response responseObj = response.take();

            // Throw a Exception if the request fails.
            if ((Integer) responseObj.getStatus() != Status.NOT_FOUND.getStatusCode())
                throw new NoSuchElementException(responseObj.getMessage().toString());
            else if ((Integer) responseObj.getStatus() != Status.ACCEPTED.getStatusCode())
                throw new IOException(responseObj.getMessage().toString());

            // Recovers the user object.
            obj = (UserObj) responseObj.getObject();

            // Cancel the consumer.
            channel.basicCancel(cTag);
        } finally {
            // Release the broker connection.
            Connections.getInstance().releaseBroker(channel);
        }

        // Return the object.
        return obj;
    }

    /**
     * Remote method for read users from users endpoint.
     * 
     * Since the Domain Driven Design architecture is being implemented in this
     * project. We will use remote methods to work with the other servers that have
     * been implemented in the project. Thus reducing the service is monolithic and
     * can be easily scalable.
     * 
     * @param id The user id.
     * @return The object of the response.
     * @throws IOException If the operation returns any exception that is not Okay, throw this exception.
     * @throws InterruptedException If the acquire process was interrupted, throws this exception.
     */
    public static UserObj readUser(Long id) throws IOException, InterruptedException {
        // Prepare the variables.
        Channel channel = null;
        UserObj obj;

        try {
            // Acquire the broker connection.
            channel = Connections.getInstance().acquireBroker();

            // Instance the properties.
            String corrId = UUID.randomUUID().toString();
            String replyTo = channel.queueDeclare().getQueue();

            // Add headers map.
            Map<String, Object> headers = new HashMap<>();
            headers.put("x-remote-method", "ReadUser");

            // Encapsulate into BasicProperties the instanced properties.
            BasicProperties props = new BasicProperties().builder().correlationId(corrId).replyTo(replyTo).headers(headers).build();

            // Build the request message.
            Request request = Request.newBuilder().setId(id).build();

            // Send the request via RPC.
            channel.basicPublish("", "users", props, request.toByteBuffer().array());

            // Prepare the response variable.
            BlockingQueue<Response> response = new ArrayBlockingQueue<>(1);

            // Create a consumer the response.
            String cTag = channel.basicConsume(replyTo, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    response.offer(Response.fromByteBuffer(ByteBuffer.wrap(delivery.getBody())));
                }
            }, consumerTag -> {
            });

            // Wait the response.
            Response responseObj = response.take();

            // Throw a Exception if the request fails.
            if ((Integer) responseObj.getStatus() != Status.NOT_FOUND.getStatusCode())
                throw new NoSuchElementException(responseObj.getMessage().toString());
            else if ((Integer) responseObj.getStatus() != Status.ACCEPTED.getStatusCode())
                throw new IOException(responseObj.getMessage().toString());

            // Recovers the user object.
            obj = (UserObj) responseObj.getObject();

            // Cancel the consumer.
            channel.basicCancel(cTag);
        } finally {
            // Release the broker connection.
            Connections.getInstance().releaseBroker(channel);
        }

        // Return the object.
        return obj;
    }
}