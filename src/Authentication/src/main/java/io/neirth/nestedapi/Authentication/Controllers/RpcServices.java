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

// Used libraries from Java Standard.
import java.io.IOException;
import java.nio.ByteBuffer;

// Used libraries for AMQP operations.
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

// Internal packages of the project.
import io.neirth.nestedapi.Authentication.Connectors.Connections;
import io.neirth.nestedapi.Authentication.Schemas.IsValidToken;
import io.neirth.nestedapi.Authentication.Schemas.Request;
import io.neirth.nestedapi.Authentication.Schemas.Response;
import io.neirth.nestedapi.Authentication.Templates.Country;
import io.neirth.nestedapi.Authentication.Templates.User;

public class RpcServices implements IsValidToken {
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
            String type = (String) delivery.getProperties().getHeaders().get("x-remote-method");

            // Read the request.
            Request request = Request.fromByteBuffer(ByteBuffer.wrap(delivery.getBody()));

            // Prepare the response.
            Response response = null;

            // Depending of the method called, we route the petition into the real method.
            switch (type) {
                case "IsValidToken":
                    response = IsValidToken(request);
                    break;
            }

            // Prepare the properties of the response.
            BasicProperties replyProps = new BasicProperties.Builder()
                    .correlationId(delivery.getProperties().getCorrelationId()).build();

            // Publish the response into the private queue and sets the acknowledge.
            channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps,
                    response.toByteBuffer().array());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        } catch (Exception e) {
            // In the case of crash, print the stack trace.
            e.printStackTrace();
        }
    }

    @Override
    public Response IsValidToken(Request request) {
        // TODO Auto-generated method stub
        return null;
    }
}