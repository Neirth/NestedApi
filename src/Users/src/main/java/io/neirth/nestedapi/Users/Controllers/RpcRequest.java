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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.ws.rs.core.Response.Status;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

import io.neirth.nestedapi.Users.Connectors.Connections;
import io.neirth.nestedapi.Users.Schemas.Request;
import io.neirth.nestedapi.Users.Schemas.Response;

public class RpcRequest {
    public static boolean isValidToken(String token) throws IOException {
        // Prepare the variables.
        Channel channel = null;
        boolean result = false;

        try {
            // Acquire the broker connection.
            channel = Connections.getInstance().acquireBroker();

            // Instance the properties.
            String corrId = UUID.randomUUID().toString();
            String replyTo = channel.queueDeclare().getQueue();

            // Add headers map.
            Map<String, Object> headers = new HashMap<>();
            headers.put("x-remote-method", "IsValidToken");

            // Encapsulate into BasicProperties the instanced properties.
            BasicProperties props = new BasicProperties().builder().correlationId(corrId).replyTo(replyTo).headers(headers).build();

            // Build the request message.
            Request request = Request.newBuilder().setToken(token).build();

            // Send the request via RPC.
            channel.basicPublish("", "auth", props, request.toByteBuffer().array());

            // Prepare the response variable.
            BlockingQueue<Response> response = new ArrayBlockingQueue<>(1);

            // Create a consumer the response.
            String ctag = channel.basicConsume(replyTo, true, (consumerTag, delivery) -> {
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
            channel.basicCancel(ctag);

            // Save the result in the variable.
            result = (Boolean) responseObj.getObject();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Release the broker connection.
            Connections.getInstance().releaseBroker(channel);
        }

        // Return the result.
        return result;
    }

    public static void removeToken(String token) throws IOException {
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
            headers.put("x-remote-method", "RemoveToken");

            // Encapsulate into BasicProperties the instanced properties.
            BasicProperties props = new BasicProperties().builder().correlationId(corrId).replyTo(replyTo).headers(headers).build();

            // Build the request message.
            Request request = Request.newBuilder().setToken(token).build();

            // Send the request via RPC.
            channel.basicPublish("", "auth", props, request.toByteBuffer().array());

            // Prepare the response variable.
            BlockingQueue<Response> response = new ArrayBlockingQueue<>(1);

            // Create a consumer the response.
            String ctag = channel.basicConsume(replyTo, true, (consumerTag, delivery) -> {
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
            channel.basicCancel(ctag);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Release the broker connection.
            Connections.getInstance().releaseBroker(channel);
        }
    }
}