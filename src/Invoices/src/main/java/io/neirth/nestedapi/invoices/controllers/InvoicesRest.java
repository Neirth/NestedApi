/*
 * MIT License
 *
 * Copyright (c) 2020 NestedApi Project
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
package io.neirth.nestedapi.invoices.controllers;

// Used libraries from Java Standard.
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;

// Used libraries from Java Enterprise.
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import org.jboss.resteasy.spi.HttpRequest;

// Internal packages of the project.
import io.neirth.nestedapi.invoices.ServiceUtils;
import io.neirth.nestedapi.invoices.connectors.Connections;
import io.neirth.nestedapi.invoices.connectors.InvoicesConn;
import io.neirth.nestedapi.invoices.templates.Invoice;

@Path("/invoices")
public class InvoicesRest {
    @GET
    @Path("{param_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context final HttpRequest req, @PathParam("param_id") String paramId) {
        return ServiceUtils.processRequest(req, paramId, null, () -> {
            // Prepare the response
            ResponseBuilder response = null;

            // Try to acquire the connection.
            InvoicesConn conn = Connections.getInstance().acquireInvoice();

            try {
                // Initialize the json object builder.
                JsonObjectBuilder jsonResponse = Json.createObjectBuilder();

                // Try to read the invoice information.
                Invoice invoice = conn.read(paramId);

                // Add the properties from the invoice object.
                jsonResponse.add("id", invoice.getId());
                jsonResponse.add("userId", invoice.getUserId());
                jsonResponse.add("creationDate", (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")).format(invoice.getCreationDate()));
                jsonResponse.add("deliveryAddress", invoice.getDeliveryAddress());
                jsonResponse.add("deliveryPostcode", invoice.getDeliveryPostcode());
                jsonResponse.add("deliveryCountry", invoice.getDeliveryCountry().name());
                jsonResponse.add("deliveryCurrency", invoice.getDeliveryCurrency().getCurrencyCode());
                jsonResponse.add("deliveryAddressInformation", invoice.getDeliveryAddressInformation());

                // We create the products json array.
                JsonArrayBuilder productsArray = Json.createArrayBuilder();

                // Insert the products inside json array.
                invoice.getProducts().forEach((value) -> {
                    JsonObjectBuilder product = Json.createObjectBuilder();
                    product.add("productName", value.getProductName());
                    product.add("productPrice", value.getProductPrice());
                    product.add("productAmount", value.getProductAmount());

                    productsArray.add(product);
                });

                // Add array to main json.
                jsonResponse.add("products", productsArray.build());

                // If the process is complete successfully, write a ok response.
                response = Response.status(Status.OK).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
            } catch (NoSuchElementException e) {
                // If the user was not found, write a not found response.
                response = Response.status(Status.NOT_FOUND);
            } catch (IllegalArgumentException e) {
                // If the hex id is not valid, write a bad request response.
                response = Response.status(Status.BAD_REQUEST);
            } finally {
                // Return the invoices connection.
                Connections.getInstance().releaseInvoice(conn);
            }

            return response;
        });
    }
}