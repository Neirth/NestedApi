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