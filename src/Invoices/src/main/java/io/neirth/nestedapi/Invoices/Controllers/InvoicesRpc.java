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
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

// Used libraries from Java Enterprise.
import javax.xml.bind.DatatypeConverter;

// Used libraries for AMQP operations.
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

// Internal packages of the project.
import io.neirth.nestedapi.Invoices.ServiceUtils;
import io.neirth.nestedapi.Invoices.Connectors.Connections;
import io.neirth.nestedapi.Invoices.Connectors.InvoicesConn;
import io.neirth.nestedapi.Invoices.Templates.Country;
import io.neirth.nestedapi.Invoices.Templates.Invoice;
import io.neirth.nestedapi.Invoices.Templates.Product;

public class InvoicesRpc {
    public void routeDelivery(Channel channel, Delivery delivery) {
        // Obtain a called method.
        String type = (String) delivery.getProperties().getHeaders().get("x-remote-method");

        // Prepare the response.
        byte[] response = null;

        try {
            // Depending of the method called, we route the petition into the real method.
            switch (type) {
                case "CreateInvoice":
                    response = createInvoice(delivery);
                    break;
                case "ReadInvoice":
                    response = readInvoice(delivery);
                    break;
            }

            // Prepare the properties of the response.
            BasicProperties replyProps = new BasicProperties.Builder()
                    .correlationId(delivery.getProperties().getCorrelationId()).build();

            // Publish the response into the private queue and sets the acknowledge.
            channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response);
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        } catch (Exception e) {
            // In the case of crash, print the stack trace.
            e.printStackTrace();
        }
    }

    private byte[] createInvoice(Delivery delivery) throws Exception {
        return ServiceUtils.processMessage(delivery, "CreateInvoice", (consumedDatum) -> {
            InvoicesConn conn = Connections.getInstance().acquireInvoice();

            try {
                List<Product> products = new ArrayList<>();
                // TODO: Recover the products list from invoice.

                Invoice invoice = new Invoice.Builder(null)
                            .setUserId((Long) consumedDatum.get("userId"))
                            .setCreationDate(DatatypeConverter.parseDateTime((String) consumedDatum.get("creationDate")).getTime())
                            .setDeliveryAddress((String) consumedDatum.get("deliveryAddress"))
                            .setDeliveryCountry(Enum.valueOf(Country.class, (String) consumedDatum.get("deliveryCountry")))
                            .setDeliveryCurrency(Currency.getInstance((String) consumedDatum.get("deliveryCurrency")))
                            .setDeliveryPostcode((String) consumedDatum.get("deliveryPostcode"))
                            .setDeliveryAddressInformation((String) consumedDatum.get("deliveryAddressInformation"))
                            .setProducts(products)
                            .build();

                conn.create(invoice);           
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Connections.getInstance().releaseInvoice(conn);
            }

            return null;
        });
    }

    private byte[] readInvoice(Delivery delivery) throws Exception {
        return ServiceUtils.processMessage(delivery, "ReadInvoice", (consumedDatum) -> {
            InvoicesConn conn = Connections.getInstance().acquireInvoice();

            Invoice invoice = null;

            try {
                invoice = conn.read((String) consumedDatum.get("id"));
            } finally {
                Connections.getInstance().releaseInvoice(conn);
            }

            return invoice;
        });
    }
}