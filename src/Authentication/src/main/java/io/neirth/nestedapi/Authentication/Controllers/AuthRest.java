package io.neirth.nestedapi.Authentication.Controllers;

import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;

import io.neirth.nestedapi.Authentication.ServiceUtils;

@Path("/auth")
public class AuthRest {
    @POST
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authToken(@Context final HttpServletRequest req, Map<String, String> body) {
        return ServiceUtils.processRequest(req, null, body, () -> {
            // TODO: Check if refresh token is present.

            // Convert Object to Map.
            Map<String, String> bodyMap = (Map<String, String>) body;


            return null;
        });
    }

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(@Context final HttpServletRequest req, String body) {
        return ServiceUtils.processRequest(req, null, body, () -> {
            String jsonRequest = (String) body;

            return null;
        });
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response logoutUser(@Context final HttpServletRequest req, String body) {
        return ServiceUtils.processRequest(req, null, body, () -> {
            String jsonRequest = (String) body;


            return null;
        });
    }
}