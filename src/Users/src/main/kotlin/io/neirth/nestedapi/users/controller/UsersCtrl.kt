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
import io.neirth.nestedapi.users.repository.UsersRepo
import io.neirth.nestedapi.users.util.annotation.RpcMessage
import io.neirth.nestedapi.users.util.processJwtToken
import javax.ws.rs.*
import javax.inject.Inject

@Path("/users")
class UsersCtrl {
    @Inject
    internal lateinit var conn : UsersRepo

    @GET
    @Path("me")
    fun getUserInfo(@HeaderParam("authorization") jwtToken : String?) : User? {
        return conn.findById(processJwtToken(jwtToken)["sub"] as Long)
    }

    @PUT
    @Path("me")
    fun updateUserInfo(@HeaderParam("authorization") jwtToken : String?, user: User) : User? {
        val jwtTokenMap : Map<String, Any?> = processJwtToken(jwtToken)

        if (user.id == jwtTokenMap["sub"]) {
            return conn.update(user)
        } else {
            throw SecurityException("Attempt to manipulate a different user")
        }
    }

    @DELETE
    @Path("me")
    fun deleteUserInfo(@HeaderParam("authorization") jwtToken: String?) {
        conn.remove(conn.findById(processJwtToken(jwtToken)["sub"] as Long))
    }

    @RpcMessage(topic = "users", queue = "register")
    fun addUserInfo(user: User) : User {
        return conn.insert(user)
    }

    @RpcMessage(topic = "users", queue = "login")
    fun getUserInfo(idUser: Long) : User {
        return conn.findById(idUser)
    }
}