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

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import java.lang.IllegalArgumentException
import java.net.MalformedURLException
import java.util.NoSuchElementException
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class ExceptionMapping : ExceptionMapper<Exception> {
    /**
     * Method to process handled exceptions and generate response
     *
     * @param p0 The handled exception
     * @return Response generated
     */
    override fun toResponse(p0: Exception): Response {
        return when (p0) {
            is SecurityException -> Response.status(Response.Status.FORBIDDEN.statusCode).entity(generateJsonResponse("access_denied", p0.message)).build()
            is LoginException -> Response.status(Response.Status.UNAUTHORIZED.statusCode).entity(generateJsonResponse("access_denied", p0.message)).build()
            is MalformedURLException -> Response.status(Response.Status.BAD_REQUEST.statusCode).entity(generateJsonResponse("invalid_request", p0.message)).build()
            is IllegalArgumentException -> Response.status(Response.Status.BAD_REQUEST.statusCode).entity(generateJsonResponse("invalid_request", p0.message)).build()
            is JsonMappingException -> Response.status(Response.Status.BAD_REQUEST.statusCode).entity(generateJsonResponse("invalid_request", "Error deserializing the body document, check the document before try again...")).build()
            is NoSuchElementException -> Response.status(Response.Status.NOT_FOUND.statusCode).entity(generateJsonResponse("resource_not_found", p0.message)).build()
            else -> {
                p0.printStackTrace()
                Response.status(Response.Status.INTERNAL_SERVER_ERROR.statusCode).entity(generateJsonResponse("server_error", p0.message)).build()
            }
        }
    }

    /**
     * Private method for generate error json response
     *
     * @param errorTitle Resource error title
     * @param errorMessage Resource error message
     * @return Encoded JSON Resource
     */
    private fun generateJsonResponse(errorTitle: String, errorMessage: String?): String {
        // Instance a ObjectMapper
        val mapper = ObjectMapper()

        // Set properties into JSON
        val user: ObjectNode = mapper.createObjectNode()
        user.put("error", errorTitle)
        user.put("error_description", errorMessage)

        // Convert JSON Node into JSON String
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user)
    }
}