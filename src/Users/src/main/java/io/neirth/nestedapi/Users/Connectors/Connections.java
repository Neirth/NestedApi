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
package io.neirth.nestedapi.Users.Connectors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Class that regulates connections to the database.
 * 
 * Since there are environments where connections to the database are limited
 * (for example due to lack of hardware or limitations imposed by the database
 * provider). This class allows, with proper regulation, to use multitasking to
 * speed up actions with the database. Thus avoiding the collapse of all threads
 * attacking the same connection.
 */
public class Connections {
    private static Connections instance = null;

    private final int maxConnections = Integer.valueOf(System.getenv("NESTEDAPI_MAX_CONNECTIONS"));

    private Semaphore userSemaphore = new Semaphore(maxConnections);

    private List<UsersConn> usersMgtList = new ArrayList<>();

    private Connections() throws SQLException {
        for (int i = 0; i < maxConnections; i++) {
            usersMgtList.add(new UsersConn(instanceConnection()));
        }
    }

    /**
     * Method for acquire a user connection.
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
    public UsersConn acquireUsers() throws InterruptedException {
        userSemaphore.acquire();

        UsersConn connection = usersMgtList.get(0);
        usersMgtList.remove(0);

        return connection;
    }

    /**
     * Method for release a user connection.
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
    public void releaseUsers(UsersConn connection) {
        usersMgtList.add(connection);

        userSemaphore.release();
    }

    /**
     * Private method for instance a database connection.
     * 
     * This is used for instance a connection object, which previously loads the
     * database squema into him. It's a private method because the only point which
     * should be used is only in the instance of UsersConn.
     * 
     * @return The database connection.
     * @throws SQLException Maybe, if the instance founds a error with the database,
     *                      he throws a exception.
     */
    private Connection instanceConnection() throws SQLException {
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
            st.execute("CREATE TABLE IF NOT EXISTS Users (id BIGSERIAL, name VARCHAR(50) NOT NULL, surname VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, password VARCHAR(32) NOT NULL, telephone TEXT, birthday DATE NOT NULL, country VARCHAR(2) NOT NULL, address TEXT, addressInformation TEXT, PRIMARY KEY(id));");
        }

        // When the connection instance is prepared, is a moment to return him to the caller method.
        return conn;
    }

    /**
     * Obtains the only instance for this class.
     * 
     * @return The class instance.
     * @throws SQLException Maybe, if the instance founds a error with the database,
     *                      he throws a exception.
     */
    public Connections getInstance() throws SQLException {
        if (instance == null)
            instance = new Connections();

        return instance;
    }
}