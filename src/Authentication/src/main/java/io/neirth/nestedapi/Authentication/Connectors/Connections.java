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
package io.neirth.nestedapi.Authentication.Connectors;

// Used libraries from Java Standard.
import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Stack;
import java.util.concurrent.Semaphore;

// Used libraries for AMQP operations.
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

// Internal packages of the project.
import io.neirth.nestedapi.Authentication.Controllers.RpcServices;

/**
 * Class that regulates connections to the database.
 * 
 * Since there are environments where connections to the database are limited
 * (for example due to lack of hardware or limitations imposed by the database
 * provider). This class allows, with proper regulation, to use multitasking to
 * speed up actions with the database. Thus avoiding the collapse of all threads
 * attacking the same connection.
 */
public class Connections implements Closeable {
    private static Connections instance = null;

    // Variable of max connections.
    private final int maxConnections = Integer.valueOf(System.getenv("NESTEDAPI_MAX_CONNECTIONS"));

    // Semaphores for connections.
    private final Semaphore tokenSemaphore = new Semaphore(maxConnections);
    private final Semaphore brokerSemaphore = new Semaphore(maxConnections);

    // Lists of connections.
    private final Stack<TokensConn> tokensConnStack = new Stack<>();
    private final Stack<Channel> brokerStack = new Stack<>();

    /**
     * Constructor with security level in default package so that it can only be
     * instantiated by the connections class.
     */
    private Connections() throws Exception {
        for (int i = 0; i < maxConnections; i++) {
            tokensConnStack.push(instanceConnection());
            brokerStack.push(instanceChannel());
        }
    }

    /**
     * Method for acquire a authentication connection.
     * 
     * If the connection doesn't avaiable, the thread caller will stopped util one
     * connection is avaiable for use.
     * 
     * The mechanism used for know who needs the connection is FIFO (First Input,
     * First Out).
     * 
     * @return The connection of Users Table.
     * @throws InterruptedException In the case of the operation was interrupted,
     *                              throws a exception.
     */
    public TokensConn acquireAuths() throws InterruptedException {
        tokenSemaphore.acquire();
        return tokensConnStack.pop();
    }

    /**
     * Method for release a authentication connection.
     * 
     * When the thread finish the operations with the connection, they must release
     * the connection for other threads use the connection.
     * 
     * If this does not happen, the server will very likely be blocked after
     * exceeding the limit number defined in the environment variable of
     * NESTEDAPI_MAX_CONNECTIONS, with no chance of recovering in production. Be
     * careful with this fact.
     * 
     * @param connection The user connection.
     */
    public void releaseAuths(TokensConn conn) {
        tokensConnStack.push(conn);
        tokenSemaphore.release();
    }

    /**
     * Method for acquire a broker connection.
     * 
     * Since the broker will need to operate constantly, it will need to have
     * several connections available. So we will need to have several connections
     * available to improve the response times of our services.
     * 
     * @return The broker connection.
     * @throws InterruptedException In the case of the operation was interrupted,
     *                              throws a exception.
     */
    public Channel acquireBroker() throws InterruptedException {
        brokerSemaphore.acquire();
        return brokerStack.pop();
    }

    /**
     * Method for release a broker connection.
     * 
     * When the thread finish the operations with the connection, they must release
     * the connection for other threads use the connection.
     * 
     * If this does not happen, the server will very likely be blocked after
     * exceeding the limit number defined in the environment variable of
     * NESTEDAPI_MAX_CONNECTIONS, with no chance of recovering in production. Be
     * careful with this fact.
     * 
     * @param conn The broker connection.
     */
    public void releaseBroker(Channel conn) {
        brokerStack.push(conn);
        brokerSemaphore.release();
    }

    /**
     * Private method for instance a database connection.
     * 
     * This is used for instance a connection object, which previously loads the
     * database squema into him. It's a private method because the only point which
     * should be used is only in the instance of TokensConn.
     * 
     * @return The database connection.
     * @throws SQLException Maybe, if the instance founds a error with the database,
     *                      he throws a exception.
     */
    private TokensConn instanceConnection() throws Exception {
        // We try to load the database driver.
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Postgres driver not found " + e.toString());
        }

        // If the database driver was loaded, try to get a connection.
        Connection conn = DriverManager.getConnection(System.getenv("JDBC_DATABASE_URL"));

        // When the connection was instanced, before return to the caller method, we
        // initialize the datbase squema, in this case, only initialize the user table.
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS Tokens (token VARCHAR(36), userId BIGINT, validFrom DATE, tokenAgent TEXT, PRIMARY KEY(token));");
        }

        // When the connection instance is prepared, is a moment to return him to the
        // caller method.
        return new TokensConn(conn);
    }

    /**
     * Method that initialize a preconfigured instance of a RabbitMQ Channel.
     * 
     * Since in order to efficiently process the requests that occur in the service.
     * Let's prepare how a channel should work and then initialize the channels that
     * we think are necessary.
     * 
     * @return A channel instance.
     */
    private Channel instanceChannel() throws Exception {
        // Sets the queue name.
        String queueName = "auth";

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

    /**
     * Dummy method to initialize the components inside Connections class
     * constructor.
     */
    public void init() {
        return;
    }

    /**
     * Obtains the only instance for this class.
     * 
     * @return The class instance.
     * @throws Exception Any exception throwed.
     */
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
            // Close all connections from this class.
            for (int i = 0; i < maxConnections; i++) {
                tokensConnStack.pop().close();
                brokerStack.pop().close();
            }
            
            // Set the instance variable equals a null.
            instance = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}