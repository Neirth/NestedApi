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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

public class UsersRpc {
    // TODO: Read the tutorial for still implemeting: https://www.rabbitmq.com/tutorials/tutorial-six-java.html
    // TODO: Maybe its is useful too: https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/RPCServer.java and https://github.com/rabbitmq/rabbitmq-tutorials/blob/master/java/RPCClient.java 
    public void parseDelivery(Channel channel, Delivery delivery) {

    }
}