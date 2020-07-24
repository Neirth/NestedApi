package io.neirth.nestedapi.Authentication.Connectors;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

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
        Token tokenObj = null;

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
    public void delete(Token token) throws SQLException {
        // Set the token of the row.
        deleteQuery.setString(1, token.getToken());

        // Check if the row was update correctly
        if (deleteQuery.executeUpdate() == 0)
            // If the case where the item doesn't exist, throws a exception warning for this
            // situation.
            throw new NoSuchElementException("The element " + token.getToken() + " is not available in the database.");
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