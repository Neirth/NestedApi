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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

import io.neirth.nestedapi.Authentication.Connectors.Connections;
import io.neirth.nestedapi.Authentication.Schemas.Request;
import io.neirth.nestedapi.Authentication.Schemas.Response;
import io.neirth.nestedapi.Authentication.Schemas.UserObj;

public class RpcRequest {
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
     * @throws InterruptedException
     * @throws IOException
     */
    public UserObj readUser(Long id) throws Exception {
        // Prepare the variables.
        Channel channel = null;
        UserObj obj;

        try {
            // Acquire the broker connection.
            channel = Connections.getInstance().acquireBroker();

            // Instance the properties.
            String corrId = UUID.randomUUID().toString();
            String replyTo = channel.queueDeclare().getQueue();

            // Encapsulate into BasicProperties the instanced properties.
            BasicProperties props = new BasicProperties()
                                        .builder()
                                        .correlationId(corrId)
                                        .replyTo(replyTo)
                                        .build();
            
            // Build the request message.
			Request request = Request.newBuilder().setId(id).build();
            
            // Send the request via RPC.
            channel.basicPublish("", "users", props, request.toByteBuffer().array());

            // Prepare the response variable.
            BlockingQueue<UserObj> response = new ArrayBlockingQueue<>(1);

            // Create a consumer the response.
            String ctag = channel.basicConsume(replyTo, true, (consumerTag, delivery) -> {
                if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                    Response responseObj = Response.fromByteBuffer(ByteBuffer.wrap(delivery.getBody()));
                    response.offer((UserObj) responseObj.getObject());
                }
            }, consumerTag -> { });
            
            // Wait the response.
            obj = response.take();

            // Cancel the consumer.
            channel.basicCancel(ctag);
        } finally {
            // Release the broker connection.
            Connections.getInstance().releaseBroker(channel);
        }
        
        // Return the object.
        return obj;
    }
}