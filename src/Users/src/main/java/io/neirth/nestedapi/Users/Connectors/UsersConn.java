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

// The sql and exceptions packages used in this class
import java.io.Closeable;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

// Internal classes of the project.
import io.neirth.nestedapi.Users.Templates.Country;
import io.neirth.nestedapi.Users.Templates.User;

/**
 * Class that manages all operations with the users table in the database.
 */
public class UsersConn implements Closeable {
    private final Connection conn;
    private final PreparedStatement createQuery;
    private final PreparedStatement readQuery;
    private final PreparedStatement updateQuery;
    private final PreparedStatement deleteQuery;

    /**
     * Constructor with security level in default package so that it can only be
     * instantiated by the connections class.
     */
    UsersConn(Connection conn) throws SQLException {
        this.conn = conn;
        this.createQuery = conn.prepareStatement("INSERT INTO Users (name, surname, email, password, telephone, birthday, country, address, addressInformation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id;");
        this.readQuery = conn.prepareStatement("SELECT * FROM Users WHERE id = ?;");
        this.updateQuery = conn.prepareStatement("UPDATE Users SET name = ?, surname = ?, email = ?, password = ?, telephone = ?, birthday = ?, country = ?, address = ?, addressInformation = ? WHERE id = ?;");
        this.deleteQuery = conn.prepareStatement("DELETE FROM Users WHERE id = ?;");
    }

    /**
     * Method to insert in the database a row with user information.
     * 
     * @param user The user to insert.
     * @return The new id of the user row.
     * @throws SQLException The exception in case of problems with the database.
     */
    public long create(User user) throws SQLException {
        // Instance the new id.
        long idCreated = 0;

        // Insert the values into update query.
        createQuery.setString(1, user.getName());
        createQuery.setString(2, user.getSurname());
        createQuery.setString(3, user.getEmail());
        createQuery.setString(4, user.getPassword());
        createQuery.setString(5, user.getTelephone());
        createQuery.setDate(6, new Date(user.getBirthday().getTime()));
        createQuery.setString(7, user.getCountry().name());
        createQuery.setString(8, user.getAddress());
        createQuery.setString(9, user.getAddressInformation());

        // Execute the query
        try (ResultSet rs = createQuery.executeQuery()) {
            if (rs.next())
                // Read the new id value and set the value into a variable.
                idCreated = rs.getLong("id");
            else
                // In the case that the new row cannot be inserted, we will manually throw the
                // exception.
                throw new SQLException("Error inserting the new user row");
        }

        // Return the id of the new row.
        return idCreated;
    }

    /**
     * Method to read a row corresponding to the users table.
     * 
     * @param user The user to read.
     * @throws SQLException           The exception in case of problems with the
     *                                database.
     * @throws NoSuchElementException The exception in the case that the desired
     *                                object is not available.
     */
    public User read(long id) throws SQLException {
        // Create a variable with null value.
        User user = null;

        // Set the id of the row.
        readQuery.setLong(1, id);

        // Read the information in the database.
        try (ResultSet rs = readQuery.executeQuery()) {
            if (rs.next())
                // We build a new user object with the database information.
                user = new User.Builder(id).setName(rs.getString("name")).setSurname(rs.getString("surname"))
                        .setEmail(rs.getString("email")).setPassword(rs.getString("password"))
                        .setTelephone(rs.getString("telephone")).setBirthday(rs.getDate("birthday"))
                        .setCountry(Enum.valueOf(Country.class, rs.getString("country")))
                        .setAddress(rs.getString("address")).setAddressInformation(rs.getString("addressInformation"))
                        .build();
            else
                // If the case where the item doesn't exist, throws a exception warning for this
                // situation.
                throw new NoSuchElementException("The element " + id + " is not available in the database.");
        }

        // Return the user object builded.
        return user;
    }

    /**
     * Method to update a row corresponding to the users table.
     * 
     * @param user The user to update.
     * @throws SQLException           The exception in case of problems with the
     *                                database.
     * @throws NoSuchElementException The exception in the case that the desired
     *                                object is not available.
     */
    public void update(User user) throws SQLException {
        // Insert the values into update query.
        updateQuery.setString(1, user.getName());
        updateQuery.setString(2, user.getSurname());
        updateQuery.setString(3, user.getEmail());
        updateQuery.setString(4, user.getPassword());
        updateQuery.setString(5, user.getTelephone());
        updateQuery.setDate(6, new Date(user.getBirthday().getTime()));
        updateQuery.setString(7, user.getCountry().name());
        updateQuery.setString(8, user.getAddress());
        updateQuery.setString(9, user.getAddressInformation());
        updateQuery.setLong(10, user.getId());

        // Check if the row was update correctly
        if (updateQuery.executeUpdate() == 0)
            // If the case where the item doesn't exist, throws a exception warning for this
            // situation.
            throw new NoSuchElementException("The element " + user.getId() + " is not available in the database.");
    }

    /**
     * Method to delete a row corresponding to the users table.
     * 
     * @param user The user to delete.
     * @throws SQLException           The exception in case of problems with the
     *                                database.
     * @throws NoSuchElementException The exception in the case that the desired
     *                                object is not available.
     */
    public void delete(User user) throws SQLException {
        // Set the id of the row.
        deleteQuery.setLong(1, user.getId());

        // Check if the row was update correctly
        if (deleteQuery.executeUpdate() == 0)
            // If the case where the item doesn't exist, throws a exception warning for this
            // situation.
            throw new NoSuchElementException("The element " + user.getId() + " is not available in the database.");
    }

    /**
     * Method to close the connections with the database.
     * 
     * @param arg0
     */
    @Override
    public void close() {
        try {
            createQuery.close();
            readQuery.close();
            updateQuery.close();
            deleteQuery.close();

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}