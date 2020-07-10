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
package io.neirth.nestedapi.Invoices;

// Libraries used for data process.
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

// Libraries used from Java Enterprise.
import javax.ws.rs.core.Response.Status;

// Libraries used for AMQP operations.
import com.rabbitmq.client.Delivery;

// Libraries used in Apache Avro Serialice and Deserialice.
import org.apache.avro.Protocol;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.jboss.logging.Logger;

// Internal packages of the project.
import io.neirth.nestedapi.Invoices.Templates.Invoice;

public class ServiceUtils {
    private static Logger loggerSystem = Logger.getLogger(ServiceApp.class);
    
    public interface RestCallback {
        ResponseBuilder run() throws Exception;
    }

    public interface RpcCallback {
        Invoice run(GenericRecord consumedDatum) throws Exception;
    }

        /**
     * Method that process the request received from RESTful API and generate a response
     * in the json format.
     * 
     * This method, with the corresponding RestCallback to do the specifici tasks,
     * retrives the information from the body and executes the callback lambda, the message
     * returnted is a HTTP response with Json Body.
     * 
     * @param req The headers of http request.
     * @param paramId The paramId of the requested object.
     * @param jsonRequest The body of http request.
     * @param callback The lambda callback
     * @return The response object.
     */
    public static Response processRequest(HttpServletRequest req, String paramId, String jsonRequest, RestCallback callback) {
        // Initialize the response builder.
        ResponseBuilder response = null;

        try {
            // Run the callback.
            response = callback.run();
        } catch (Exception e) {
            // Write the exception in the log.
            writeServerException(e);

            // Create error response.
            response = Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
        }

        // Throws the response.
        return response.build();
    }

    /**
     * Method that process the messages received from Apache Avro and generates a
     * response in the same format.
     * 
     * This method, with the corresponding RpcCallback to do the specific tasks,
     * retrieves the information from the message, the RpcCallback receives it as a
     * parameter and this method waits for all operations to finish before encoding
     * the response in the Apache Avro format.
     * 
     * @param delivery     The request message.
     * @param protocolName The protocol used for the process.
     * @param RpcCallback  The lambda function that use for process the data.
     * @return The response message.
     * @throws Exception Throws any exception.
     */
    public static byte[] processMessage(Delivery delivery, String protocolName, RpcCallback callback) throws Exception {
        // Prepare the return message.
        byte[] message = null;

        // Obtain a Protocol Schemas
        Protocol protocol = Protocol.parse(ServiceUtils.loadAvroProtocolFile(protocolName));

        // Prepare the response message.
        GenericRecord response = new GenericData.Record(protocol.getType("Response"));

        try {
            // Prepare the generic schema reader.
            DatumReader<GenericRecord> consumer = new GenericDatumReader<>(protocol.getType("Request"));

            // Decode the binary message into a readable for the reader.
            InputStream consumedByteArray = new ByteArrayInputStream(delivery.getBody());
            Decoder consumedDecoder = DecoderFactory.get().binaryDecoder(consumedByteArray, null);

            // Convert the Decoder data to GenericRecord data.
            GenericRecord consumedDatum = consumer.read(null, consumedDecoder);

            // Run the RpcCallback for this operation.
            Invoice invoice = callback.run(consumedDatum);

            // Prepare the return message.
            response.put("status", Status.OK.getStatusCode());
            response.put("message", null);

            // In case of the operation return a object, serialize the object to return.
            if (invoice != null && invoice instanceof Invoice) {
                // Instance a new GenericRecord object.
                GenericRecord invoiceEncoded = new GenericData.Record(protocol.getType("User"));

                // Serialize the user to return it.
                invoiceEncoded.put("id", invoice.getId());
                invoiceEncoded.put("userId", invoice.getUserId());
                invoiceEncoded.put("creationDate", (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")).format(invoice.getCreationDate()));
                invoiceEncoded.put("deliveryAddress", invoice.getDeliveryAddress());
                invoiceEncoded.put("deliveryPostcode", invoice.getDeliveryPostcode());
                invoiceEncoded.put("deliveryCountry", invoice.getDeliveryCountry().name());
                invoiceEncoded.put("deliveryCurrency", invoice.getDeliveryCurrency().getCurrencyCode());
                invoiceEncoded.put("deliveryAddressInformation", invoice.getDeliveryAddressInformation());

                // Put the generic record to the main GenericRecord.
                response.put("object", invoiceEncoded);
            } else {
                response.put("object", null);
            }
        } catch (NoSuchElementException e) {
            // Prepare the return message.
            response.put("status", Status.NOT_FOUND.getStatusCode());
            response.put("message", e.getMessage());
            response.put("object", null);
        } catch (Exception e) {
            // Write the log
            writeServerException(e);

            // Prepare the return message.
            response.put("status", Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.put("message", e.getMessage());
            response.put("object", null);
        } finally {
            // Prepare the writer for generate a response.
            DatumWriter<GenericRecord> responser = new GenericDatumWriter<>(protocol.getType("Response"));

            // Prepare the output stream.
            OutputStream out = new ByteArrayOutputStream();
            Encoder binaryEncoder = EncoderFactory.get().binaryEncoder(out, null);

            // Write the message into output stream.
            responser.write(response, binaryEncoder);

            // Flush the encoder buffer.
            binaryEncoder.flush();

            // Return the data into the router.
            message = ((ByteArrayOutputStream) out).toByteArray();
        }

        // Return the response message.
        return message;
    }

    
    /**
     * Method to write the exception in the logs.
     * 
     * It's useful for write the stack trace in debug mode or only the error in
     * production.
     * 
     * @param Exception The exception catched.
     */
    public static void writeServerException(Exception e) {
        if (ServiceUtils.getLoggerSystem().isDebugEnabled()) {
            // If the log level is set to debug, write the trace stack.
            ServiceUtils.getLoggerSystem().debug("An exception has occurred, getting the stacktrace of the exception: ");
            e.printStackTrace();
        } else {
            // Information for production log.
            ServiceUtils.getLoggerSystem().error("An exception has occurred, " + e.toString());
        }
    }

    /**
     * Private method to get Apache Avro protocol passed by parameter.
     * 
     * @param protocol The protocol filename
     * @return The input stream
     * @throws IOException Ocurrs if the data is unaccesible.
     */
    private static InputStream loadAvroProtocolFile(String protocol) throws IOException {
        // We obtain the current thread context.
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        // Return the protocol as the stream.
        return loader.getResourceAsStream("/io/neirth/nestedapi/Users/Schemas/" + protocol + ".avpr");
    }

    /**
     * Method to access the event log of this module.
     * 
     * @return The logger instance.
     */
    public static Logger getLoggerSystem() {
        return loggerSystem;
    }

	public static Response processRequest(HttpServletRequest req, long paramId, Object object) {
		return null;
	}

}