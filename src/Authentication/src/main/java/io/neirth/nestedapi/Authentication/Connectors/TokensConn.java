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
import java.util.NoSuchElementException;

// Used libraries for SQL Queries.
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

// Internal packages of the project.
import io.neirth.nestedapi.Authentication.Templates.Token;

public class TokensConn implements Closeable {
    private final Connection conn;
    private final PreparedStatement createQuery;
    private final PreparedStatement readQuery;
    private final PreparedStatement deleteQuery;

    /**
     * Constructor with security level in default package so that it can only be
     * instantiated by the connections class.
     */
    TokensConn(Connection conn) throws SQLException {
        this.conn = conn;
        this.createQuery = conn.prepareStatement("INSERT INTO Tokens (userId, validFrom, token, tokenAgent) VALUES (?, ?, ?, ?);");
        this.readQuery = conn.prepareStatement("SELECT * FROM Tokens WHERE token = ?;");
        this.deleteQuery = conn.prepareStatement("DELETE FROM Tokens WHERE token = ?;");
    }

    /**
     * Method to insert in the database a row with token information.
     * 
     * @param token The token to insert.
     * @throws SQLException The exception in case of problems with the database.
     */
    public void create(Token token) throws SQLException {
        // Insert the values into update query.
        createQuery.setLong(1, token.getUserId());
        createQuery.setDate(2, new Date(token.getValidFrom().getTime()));
        createQuery.setString(3, token.getToken());
        createQuery.setString(4, token.getUserAgent());

        // Execute the query
        createQuery.executeQuery();
    }

    /**
     * Method to read a row corresponding to the tokens table.
     * 
     * @param token The token to read.
     * @throws SQLException           The exception in case of problems with the
     *                                database.
     * @throws NoSuchElementException The exception in the case that the desired
     *                                object is not available.
     */
    public Token read(String token) throws SQLException {
        // Create a variable with null value.
        Token tokenObj;

        // Set the token of the row.
        readQuery.setString(1, token);

        // Read the information in the database.
        try (ResultSet rs = readQuery.executeQuery()) {
            if (rs.next())
                // We build a new token object with the database information.
                tokenObj = new Token.Builder().setToken(rs.getString("token")).setUserId(rs.getLong("userId"))
                                              .setUserAgent("userAgent").setValidFrom(rs.getDate("validFrom"))
                                              .build();
            else
                // If the case where the item doesn't exist, throws a exception warning for this
                // situation.
                throw new NoSuchElementException("The token " + token + " is not available in the database.");
        }

        // Return the token object builded.
        return tokenObj;
    }

    /**
     * Method to check if exists a token in the tokens table.
     * @param token The token to check.
     * @return True if exists.
     * @throws SQLException           The exception in case of problems with the
     *                                database.
     */
    public boolean exists(String token) throws SQLException {
        // Set the token of the row.
        readQuery.setString(1, token);

        // Read the information in the database.
        try (ResultSet rs = readQuery.executeQuery()) {
            // Return the value of the search, if was found, this returns true.
            return rs.next();
        }
    }

    /**
     * Method to delete a row corresponding to the tokens table.
     * 
     * @param token The token to delete.
     * @throws SQLException           The exception in case of problems with the
     *                                database.
     * @throws NoSuchElementException The exception in the case that the desired
     *                                object is not available.
     */
    public void delete(String token) throws SQLException {
        // Set the token of the row.
        deleteQuery.setString(1, token);

        // Check if the row was update correctly
        if (deleteQuery.executeUpdate() == 0)
            // If the case where the item doesn't exist, throws a exception warning for this
            // situation.
            throw new NoSuchElementException("The element " + token + " is not available in the database.");
    }

    @Override
    public void close() {
        try {
            createQuery.close();
            readQuery.close();
            deleteQuery.close();

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}