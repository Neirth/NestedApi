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
package io.neirth.nestedapi.users.controller

import io.neirth.nestedapi.users.domain.User
import io.neirth.nestedapi.users.service.UsersService
import io.neirth.nestedapi.users.util.annotation.RpcMessage
import org.eclipse.microprofile.jwt.JsonWebToken
import javax.annotation.security.RolesAllowed
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.*

@Path("me")
@ApplicationScoped
class UsersCtrl(
    private val jwt: JsonWebToken,
    private val usersService: UsersService
) {
    /**
     * HTTP Method to get the user information
     *
     * @return A User information
     */
    @GET
    @RolesAllowed("users")
    fun getUserInfo(): User? {
        return usersService.findUserById(jwt.subject.toLong())
    }

    /**
     * HTTP Method to put the new user information
     *
     * @param user The new user object
     * @return A user information updated
     */
    @PUT
    @RolesAllowed("users")
    fun updateUserInfo(user: User): User? {
        return usersService.updateUserById(jwt.subject.toLong(), user)
    }

    /**
     * HTTP Method to delete the user account
     */
    @DELETE
    @RolesAllowed("users")
    fun deleteUserInfo() {
        return usersService.deleteUserById(jwt.subject.toLong())
    }

    /**
     * RPC Method to add new users in the database
     *
     * @param user The user encapsulated
     * @return A user information
     */
    @RpcMessage(topic = "users", queue = "register")
    fun addUserInfo(user: User): User {
        return usersService.insertUserByObj(user)
    }

    /**
     * RPC Method to get a user information
     *
     * @param user The id encapsulated in user object
     * @return A possible user found
     */
    @RpcMessage(topic = "users", queue = "login")
    fun getUserInfo(user: User): User? {
        return usersService.findUserById(user.id)
    }
}