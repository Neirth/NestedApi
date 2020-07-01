package io.neirth.nestedapi.Invoices.Connectors;

import java.io.Closeable;
import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.Semaphore;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import io.neirth.nestedapi.Invoices.Controllers.InvoicesRpc;

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

    private Connections() {

    }

    public InvoicesConn acquireInvoice() throws InterruptedException {
        invoiceSemaphore.acquire();
        return invoicesConnStack.pop();
    }

    public void releaseInvoice(InvoicesConn conn) {
        invoicesConnStack.push(conn);
        invoiceSemaphore.release();
    }

    public Channel acquireChannel() throws InterruptedException {
        brokerSemaphore.acquire();
        return brokerStack.pop();
    }

    public void releaseChannel(Channel conn) {
        brokerStack.push(conn);
        brokerSemaphore.release();
    }

    private InvoicesConn instanceConnection() throws Exception {
        return null;
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
            new Thread(() -> (new InvoicesRpc()).routeDelivery(channel, delivery)).start();
        };

        // Configure channel.
        channel.queueDeclare(queueName, true, false, false, null);
        channel.basicConsume(queueName, true, deliverCallback, (consumerTag) -> {
        });

        // Return the channel for future uses.
        return channel;
    }

    public void init() {
        return;
    }

    public static Connections getInstance() {
        if (instance == null)
            instance = new Connections();

        return instance;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }
}