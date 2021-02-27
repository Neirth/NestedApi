package io.neirth.nestedapi.controller

import io.neirth.nestedapi.domain.User
import io.neirth.nestedapi.repository.UsersRepo
import io.neirth.nestedapi.util.processJwtToken
import javax.ws.rs.*

@Path("/users")
class UsersCtrl {
    private val conn : UsersRepo = UsersRepo()

    @GET
    @Path("/me")
    fun getUserInfo(@HeaderParam("authorization") jwtToken : String) : User {
        return conn.findById(processJwtToken(jwtToken)["id"] as Long)
    }

    @PUT
    @Path("/me")
    fun updateUserInfo(@HeaderParam("authorization") jwtToken : String, user: User) : User {
        val user : User = user
        val jwtToken : Map<String, Any?> = processJwtToken(jwtToken)

        if (user.id == jwtToken["id"]) {
            return conn.update(user)
        } else {
            throw SecurityException("Attempt to manipulate a different user")
        }
    }

    @DELETE
    @Path("/me")
    fun deleteUserInfo(@HeaderParam("authorization") jwtToken: String) {
        conn.remove(conn.findById(processJwtToken(jwtToken)["id"] as Long))
    }
}