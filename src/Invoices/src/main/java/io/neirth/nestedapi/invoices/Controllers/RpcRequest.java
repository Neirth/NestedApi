package io.neirth.nestedapi.invoices.controllers;

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

import io.neirth.nestedapi.invoices.connectors.Connections;
import io.neirth.nestedapi.schemas.Request;
import io.neirth.nestedapi.schemas.Response;

public class RpcRequest {
    public static boolean isValidToken(String token) throws IOException, InterruptedException {
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
                if (delivery.getProperties().getCorrelationId().equals(corrId))
                    response.offer(Response.fromByteBuffer(ByteBuffer.wrap(delivery.getBody())));
            }, consumerTag -> {});

            // Wait the response.
            Response responseObj = response.take();

            // Throw a Exception if the request fails.
            if ((Integer) responseObj.getStatus() != Status.ACCEPTED.getStatusCode())
                throw new IOException(responseObj.getMessage().toString());

            // Cancel the consumer.
            channel.basicCancel(ctag);

            // Save the result in the variable.
            result = (Boolean) responseObj.getData();
        } finally {
            // Release the broker connection.
            Connections.getInstance().releaseBroker(channel);
        }

        // Return the result.
        return result;
    }
}