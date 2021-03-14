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
import io.neirth.nestedapi.users.util.processJwtToken
import javax.ws.rs.*

@Path("/users")
class UsersCtrl(private val usersService: UsersService) {
    @GET
    @Path("me")
    fun getUserInfo(@HeaderParam("authorization") jwtToken: String?): User? {
        return usersService.findUserById(processJwtToken(jwtToken)["sub"] as Long)
    }

    @PUT
    @Path("me")
    fun updateUserInfo(@HeaderParam("authorization") jwtToken: String?, user: User): User? {
        return usersService.updateUserById(processJwtToken(jwtToken)["sub"] as Long, user)
    }

    @DELETE
    @Path("me")
    fun deleteUserInfo(@HeaderParam("authorization") jwtToken: String?) {
        return usersService.deleteUserById(processJwtToken(jwtToken)["sub"] as Long)
    }

    @RpcMessage(topic = "users", queue = "register")
    fun addUserInfo(user: User): User {
        return usersService.insertUserByObj(user)
    }

    @RpcMessage(topic = "users", queue = "login")
    fun getUserInfo(idUser: Long): User {
        return usersService.findUserById(idUser)
    }
}