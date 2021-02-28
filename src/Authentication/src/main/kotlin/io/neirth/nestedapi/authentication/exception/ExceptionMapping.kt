package io.neirth.nestedapi.authentication.exception

import java.net.MalformedURLException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class ExceptionMapping : ExceptionMapper<Exception> {
    override fun toResponse(p0: Exception): Response {
        return when (p0) {
            is SecurityException -> {
                Response.status(Response.Status.FORBIDDEN.statusCode, p0.message).build()
            }
            is LoginException -> {
                Response.status(Response.Status.UNAUTHORIZED.statusCode, p0.message).build()
            }
            is MalformedURLException -> {
                Response.status(Response.Status.BAD_REQUEST.statusCode, p0.message).build()
            }
            else -> {
                println(p0.printStackTrace())
                Response.status(Response.Status.INTERNAL_SERVER_ERROR.statusCode, p0.message).build()
            }
        }
    }
}