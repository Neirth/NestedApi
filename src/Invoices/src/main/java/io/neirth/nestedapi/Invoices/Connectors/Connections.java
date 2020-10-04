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
package io.neirth.nestedapi.Invoices.Connectors;

import java.io.Closeable;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.SocketSettings;

import io.neirth.nestedapi.Invoices.Controllers.RpcServices;

public class Connections implements Closeable {
    private static Connections instance = null;

    // Variable of max connections.
    private final int maxConnections = Integer.valueOf(System.getenv("NESTEDAPI_MAX_CONNECTIONS"));

    // Semaphores for connections.
    private final Semaphore invoiceSemaphore = new Semaphore(maxConnections);
    private final Semaphore brokerSemaphore = new Semaphore(maxConnections);

    // Stack of connections.
    private final Stack<InvoicesConn> invoicesConnStack = new Stack<>();
    private final Stack<Channel> brokerStack = new Stack<>();

    private Connections() throws Exception {
        // Open the rest of connections.
        for (int i = 0; i < maxConnections; i++) {
            invoicesConnStack.push(instanceConnection());
            brokerStack.push(instanceChannel());
        }
    }

    public InvoicesConn acquireInvoice() throws InterruptedException {
        invoiceSemaphore.acquire();
        return invoicesConnStack.pop();
    }

    public void releaseInvoice(InvoicesConn conn) {
        invoicesConnStack.push(conn);
        invoiceSemaphore.release();
    }

    public Channel acquireBroker() throws InterruptedException {
        brokerSemaphore.acquire();
        return brokerStack.pop();
    }

    public void releaseBroker(Channel conn) {
        brokerStack.push(conn);
        brokerSemaphore.release();
    }

    private InvoicesConn instanceConnection() throws Exception {
        // Set the application timeout to connect with the database.
        // FIXME: Quarkus mongodb ignores the next line.
        SocketSettings socketSettings = SocketSettings.builder().connectTimeout(3000, TimeUnit.MILLISECONDS).build();

        // Connect the Server into MongoDB.
        MongoClient conn = MongoClients.create(MongoClientSettings.builder()
                                              .retryReads(true).retryWrites(true)
                                              .applyConnectionString(new ConnectionString(System.getenv("MONGODB_URI")))
                                              .applyToSocketSettings(block -> block.applySettings(socketSettings))
                                              .build());
        
        // Forces check of the connection with simple documents count.
        conn.getDatabase(System.getenv("MONGODB_DATABASE")).getCollection("invoices").countDocuments();

        // Return the InvoicesConn with connection set.
        return new InvoicesConn(conn);
    }

    private Channel instanceChannel() throws Exception {
        // Sets the queue name.
        String queueName = "invoices";

        // Open the connection with the broker.
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(System.getenv("RABBITMQ_AMQP_URL"));

        // Open the channel and sets the callback into the channel logic.
        Channel channel = factory.newConnection().createChannel();

        // Prepare callback logic.
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            // Start a new thread with Apache Avro Parser.
            new Thread(() -> (new RpcServices()).routeDelivery(channel, delivery)).start();
        };

        // Configure channel.
        channel.queueDeclare(queueName, true, false, false, null);
        channel.basicConsume(queueName, true, deliverCallback, (consumerTag) -> { });
        
        // Return the channel for future uses.
        return channel;
    }

    public void init() {
        return;
    }

    public static Connections getInstance() {
        if (instance == null)
            try {
                instance = new Connections();
            } catch (Exception e) {
                e.printStackTrace();
            }

        return instance;
    }

    @Override
    public void close() {
        try {
            for (int i = 0; i < maxConnections; i++) {
                invoicesConnStack.pop().close();
                brokerStack.pop().close();
            }

            instance = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}