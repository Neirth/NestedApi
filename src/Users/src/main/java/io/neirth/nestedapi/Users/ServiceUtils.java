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
package io.neirth.nestedapi.Users;

// Libraries used for byte tratament.
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

// Internal packages of the project.
import io.neirth.nestedapi.Users.Templates.User;

public class ServiceUtils {
    public interface Callback {
        User run(GenericRecord consumedDatum) throws Exception;
    }

    /**
     * Method that processes the messages received from Apache Avro and generates a
     * response in the same format.
     * 
     * This method, with the corresponding callback to do the specific tasks, retrieves 
     * the information from the message, the callback receives it as a parameter and 
     * this method waits for all operations to finish before encoding the response in 
     * the Apache Avro format.
     * 
     * @param delivery The request message.
     * @param protocolName The protocol used for the process.
     * @param callback The lambda function that use for process the data.
     * @return The response message.
     * @throws Exception Throws any exception.
     */
    public static byte[] processMessage(Delivery delivery, String protocolName, Callback callback) throws Exception {
        // Prepare the return message.
        byte[] message = null;

        // Obtain a Protocol Schemas
        Protocol protocol = Protocol.parse(ServiceUtils.loadAvroProtocolFile(protocolName));

        try {
            // Prepare the generic schema reader.
            DatumReader<GenericRecord> consumer = new GenericDatumReader<>(protocol.getType("Request"));

            // Decode the binary message into a readable for the reader.
            InputStream consumedByteArray = new ByteArrayInputStream(delivery.getBody());
            Decoder consumedDecoder = DecoderFactory.get().binaryDecoder(consumedByteArray, null);

            // Convert the Decoder data to GenericRecord data.
            GenericRecord consumedDatum = consumer.read(null, consumedDecoder);

            // Run the callback for this operation.
            User user = callback.run(consumedDatum);

            // Prepare the return message.
            GenericRecord response = new GenericData.Record(protocol.getType("Response"));
            response.put("status", Status.OK.getStatusCode());
            response.put("message", null);

            // In case of the operation return a object, serialize the object to return.
            if (user != null) {
                // Instance a new GenericRecord object.
                GenericRecord userEncoded = new GenericData.Record(protocol.getType("User"));

                // Serialize the user to return it.
                userEncoded.put("id", user.getId());
                userEncoded.put("name", user.getName());
                userEncoded.put("surname", user.getSurname());
                userEncoded.put("email", user.getEmail());
                userEncoded.put("password", user.getPassword());
                userEncoded.put("telephone", user.getTelephone());
                userEncoded.put("birthday", (new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")).format(user.getBirthday()));
                userEncoded.put("country", user.getCountry().name());
                userEncoded.put("address", user.getAddress());
                userEncoded.put("addressInformation", user.getAddressInformation());

                // Put the generic record to the main GenericRecord.
                response.put("object", userEncoded);
            } else {
                response.put("object", null);
            }

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
        } catch (Exception e) {
            // Prepare the return message.
            GenericRecord response = new GenericData.Record(protocol.getType("Response"));
            response.put("status", Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.put("message", e.getMessage());
            response.put("object", null);

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
        return loader.getResourceAsStream(protocol + ".avpr");
    }
}