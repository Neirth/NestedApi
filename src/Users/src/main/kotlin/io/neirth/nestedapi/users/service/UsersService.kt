/*
 * MIT License
 *
 * Copyright (c) 2021 NestedApi Project
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
package io.neirth.nestedapi.users.service

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import io.neirth.nestedapi.users.domain.User
import io.neirth.nestedapi.users.repository.UsersRepo
import io.neirth.nestedapi.users.util.RpcUtils
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class UsersService(private val conn: UsersRepo) {
    fun insertUserByObj(user: User): User {
        return conn.insert(user)
    }

    fun findUserById(id: Long): User {
       return conn.findById(id)
    }

    fun updateUserById(id: Long, user: User): User {
        if (user.id == id) {
            return conn.update(user)
        } else {
            throw SecurityException("Attempt to manipulate a different user")
        }
    }

    fun deleteUserById(id: Long) {
        conn.remove(conn.findById(id))

        val objectMapper = ObjectMapper()
        val jsonRequest: JsonParser = objectMapper.createParser("{ \"userId\" : $id }")

        RpcUtils.sendMessage("auths.remove", objectMapper.readTree(jsonRequest)) ?: throw RuntimeException("No couldn't delete a user credentials")
    }
}