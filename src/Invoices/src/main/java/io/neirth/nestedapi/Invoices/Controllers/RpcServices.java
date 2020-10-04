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
package io.neirth.nestedapi.Invoices.Controllers;

// Used libraries from Java Standard.
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.NoSuchElementException;

// Used libraries from Java Enterprise.
import javax.persistence.EnumType;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;

// Used libraries for AMQP operations.
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

import io.neirth.nestedapi.Invoices.ServiceUtils;
// Internal packages of the project.
import io.neirth.nestedapi.Invoices.Connectors.Connections;
import io.neirth.nestedapi.Invoices.Connectors.InvoicesConn;
import io.neirth.nestedapi.Invoices.Schemas.CreateInvoice;
import io.neirth.nestedapi.Invoices.Schemas.InvoiceObj;
import io.neirth.nestedapi.Invoices.Schemas.ProductObj;
import io.neirth.nestedapi.Invoices.Schemas.ReadInvoice;
import io.neirth.nestedapi.Invoices.Schemas.Request;
import io.neirth.nestedapi.Invoices.Schemas.Response;
import io.neirth.nestedapi.Invoices.Templates.Country;
import io.neirth.nestedapi.Invoices.Templates.Invoice;

public class RpcServices implements CreateInvoice, ReadInvoice {
    public void routeDelivery(Channel channel, Delivery delivery) {
        try {
            // Obtain a called method.
            String type = (String) delivery.getProperties().getHeaders().get("x-remote-method");

            // Read the request.
            Request request = Request.fromByteBuffer(ByteBuffer.wrap(delivery.getBody()));

            // Prepare the response.
            Response response = null;

            // Depending of the method called, we route the petition into the real method.
            switch (type) {
                case "CreateInvoice":
                    response = CreateInvoice(request);
                    break;
                case "ReadInvoice":
                    response = ReadInvoice(request);
                    break;
            }

            // Prepare the properties of the response.
            BasicProperties replyProps = new BasicProperties.Builder()
                    .correlationId(delivery.getProperties().getCorrelationId()).build();

            // Publish the response into the private queue and sets the acknowledge.
            channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.toByteBuffer().array());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        } catch (Exception e) {
            // In the case of crash, print the stack trace.
            ServiceUtils.writeServerException(e);
        }
    }

    @Override
    public Response ReadInvoice(Request request) {
        // Prepare the conn and response variable
        InvoicesConn conn = null;
        Response response = new Response();

        try {
            // Prepare the database connection.
            conn = Connections.getInstance().acquireInvoice();

            // Read the invoice object.
            Invoice invoice = conn.read(request.getId().toString());

            // Insert properties from object into message.
            InvoiceObj invoiceObj = new InvoiceObj();
            invoiceObj.setId(invoice.getId());
            invoiceObj.setUserId(invoice.getUserId());
            invoiceObj.setCreationDate((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")).format(invoice.getCreationDate()));
            invoiceObj.setDeliveryAddress(invoice.getDeliveryAddress());
            invoiceObj.setDeliveryPostcode(invoice.getDeliveryPostcode());
            invoiceObj.setDeliveryCountry(invoice.getDeliveryCountry().getCountryName());
            invoiceObj.setDeliveryCurrency(invoice.getDeliveryCurrency().getCurrencyCode());
            invoiceObj.setDeliveryAddressInformation(invoice.getDeliveryAddressInformation());

            // We create the products array.
            List<ProductObj> productsArray = new ArrayList<>();

            // Insert the products inside array.
            invoice.getProducts().forEach((value) -> {
                ProductObj productObj = new ProductObj();
                productObj.setProductName(value.getProductName());
                productObj.setProductPrice(value.getProductPrice());
                productObj.setProductAmount(value.getProductAmount());

                productsArray.add(productObj);
            });

            // Set the products list into message.
            invoiceObj.setProducts(productsArray);

            // Set the ok status code
            response.put("status", Status.OK.getStatusCode());
            response.put("object", invoiceObj);
        } catch (NoSuchElementException e) {
            response.put("status", Status.NOT_FOUND.getStatusCode());
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("status", Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.put("message", e.getMessage());
        } finally {
            // Release the connection with the database.
            Connections.getInstance().releaseInvoice(conn);
        }

        // Return the response.
        return response;
    }

    @Override
    public Response CreateInvoice(Request request) {
        // Prepare the conn and response variable
        InvoicesConn conn = null;
        Response response = new Response();

        try {
            // Prepare the database connection.
            conn = Connections.getInstance().acquireInvoice();

            // Build the invoice object.
            Invoice invoice = new Invoice.Builder(null)
                    .setUserId(request.getInvoice().getUserId())
                    .setCreationDate(DatatypeConverter.parseDateTime(request.getInvoice().getCreationDate().toString()).getTime())
                    .setDeliveryAddress(request.getInvoice().getDeliveryAddress().toString())
                    .setDeliveryAddressInformation(request.getInvoice().getDeliveryAddressInformation().toString())
                    .setDeliveryCountry(EnumType.valueOf(Country.class, request.getInvoice().getDeliveryCountry().toString()))
                    .setDeliveryCurrency(Currency.getInstance(request.getInvoice().getDeliveryCurrency().toString()))
                    .setDeliveryPostcode(request.getInvoice().getDeliveryPostcode().toString())
                    .build();

            // Insert the invoice into database.
            String id = conn.create(invoice);

            // Set the ok status code
            response.put("status", Status.OK.getStatusCode());
            response.put("object_id", id);
        } catch (NoSuchElementException e) {
            response.put("status", Status.NOT_FOUND.getStatusCode());
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("status", Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.put("message", e.getMessage());
        } finally {
            // Release the connection with the database.
            Connections.getInstance().releaseInvoice(conn);
        }

        // Return the response.
        return response;
    }
}