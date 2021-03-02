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
package io.neirth.nestedapi.authentication.exception

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import java.net.MalformedURLException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider


@Provider
class ExceptionMapping : ExceptionMapper<Exception> {
    override fun toResponse(p0: Exception): Response {
        return when (p0) {
            is SecurityException -> {
                Response.status(Response.Status.FORBIDDEN.statusCode).entity(generateJsonResponse(p0)).build()
            }
            is LoginException -> {
                Response.status(Response.Status.UNAUTHORIZED.statusCode).entity(generateJsonResponse(p0)).build()
            }
            is MalformedURLException -> {
                Response.status(Response.Status.BAD_REQUEST.statusCode).entity(generateJsonResponse(p0)).build()
            }
            else -> {
                println(p0.printStackTrace())
                Response.status(Response.Status.INTERNAL_SERVER_ERROR.statusCode).entity(generateJsonResponse(p0)).build()
            }
        }
    }

    private fun generateJsonResponse(p0: Exception): String {
        val mapper = ObjectMapper()

        val user: ObjectNode = mapper.createObjectNode()
        user.put("error", "${p0.cause}")
        user.put("error_description", "${p0.message}")

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user)
    }
}