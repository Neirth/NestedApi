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
import javax.validation.ConstraintViolation
import javax.validation.ConstraintViolationException
import javax.validation.Validator

@ApplicationScoped
class UsersService(private val conn: UsersRepo, private val validator: Validator) {
    /**
     * Method for insert users using the entity object
     *
     * @param user The user entity
     * @return The entity with database values
     */
    fun insertUserByObj(user: User): User {
        val violations: Set<ConstraintViolation<User>> = validator.validate(user)

        if (violations.isEmpty()) {
            return conn.insert(user)
        } else {
            throw ConstraintViolationException(violations)
        }
    }

    /**
     * Method for find users using the id
     *
     * @param id The id used by the user
     * @return The user entity
     */
    fun findUserById(id: Long): User? {
       return conn.findById(id)
    }

    /**
     * Method for update users using the id
     *
     * @param id The user id
     * @param user The user entity
     * @return The user entity with database values
     */
    fun updateUserById(id: Long, user: User): User {
        val violations: Set<ConstraintViolation<User>> = validator.validate(user)

        if (violations.isEmpty()) {
            if (user.id == id) {
                return conn.update(user)
            } else {
                throw SecurityException("Attempt to manipulate a different user")
            }
        } else {
            throw ConstraintViolationException(violations)
        }
    }

    /**
     * Method for delete users using the id
     *
     * @param id The user id
     */
    fun deleteUserById(id: Long) {
        conn.findById(id)?.let {
            conn.remove(it)
            val objectMapper = ObjectMapper()
            val jsonRequest: JsonParser = objectMapper.createParser("{ \"userId\" : $id }")

            RpcUtils.sendMessage("auths.remove", objectMapper.readTree(jsonRequest)) ?: throw RuntimeException("No couldn't delete a user credentials")
        }
    }
}