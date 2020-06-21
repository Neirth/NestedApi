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
import java.sql.PreparedStatement;
import java.sql.SQLException;

import io.neirth.nestedapi.Users.Templates.User;

public class UsersConn {
    private final PreparedStatement createQuery;
    private final PreparedStatement readQuery;
    private final PreparedStatement updateQuery;
    private final PreparedStatement deleteQuery;

    public UsersConn(Connection conn) throws SQLException {
        this.createQuery = conn.prepareStatement("INSERT INTO Users (name, surname, email, password, telephone, birthday, country, address, addressInformation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
        this.readQuery = conn.prepareStatement("SELECT * FROM Users WHERE id = ?;");
        this.updateQuery = conn.prepareStatement("UPDATE Users SET name = ?, surname = ?, email = ?, password = ?, telephone = ?, birthday = ?, country = ?, address = ?, addressInformation = ? WHERE id = ?;");
        this.deleteQuery = conn.prepareStatement("DELETE FROM Users WHERE id = ?;");
    }

    public void create(User user) throws SQLException {
        
    }

    public User read(long id) throws SQLException {
        return null;
    }

    public User update(User user) throws SQLException {
        return null;
    }

    public void delete(User user) throws SQLException {

    }
}