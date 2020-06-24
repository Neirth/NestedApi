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
package io.neirth.nestedapi.Users.Controllers;

// Libraries used for byte tratament.
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

// Libraries used from Java Enterprise.
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;

// Libraries used for AMQP operations.
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

// Libraries used in Apache Avro Serialice and Deserialice.
import org.apache.avro.Protocol;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;

// Internal packages of the project.
import io.neirth.nestedapi.Users.Connectors.Connections;
import io.neirth.nestedapi.Users.Connectors.UsersConn;
import io.neirth.nestedapi.Users.Templates.Country;
import io.neirth.nestedapi.Users.Templates.User;

public class UsersRpc {

    /**
     * Method to receive AMQP traffic and route the correct method.
     * 
     * This method, called after the callback of the reception of a message, will
     * process all the information that has been passed by parameters to know what
     * method the request corresponds to, likewise, after the response is processed,
     * the client will be returned a operation status message.
     * 
     * @param channel  The channel where receives the petition.
     * @param delivery The petition.
     */
    public void routeDelivery(Channel channel, Delivery delivery) {
        // Obtain a called method.
        String type = (String) delivery.getProperties().getHeaders().get("remote-method-call");

        // Prepare the response.
        byte[] response = null;

        try {
            // Depending of the method called, we route the petition into the real method.
            switch (type) {
                case "CreateUser":
                    response = createUser(delivery);
                    break;
                case "ReadUser":
                    response = readUser(delivery);
                    break;
                case "UpdateUser":
                    response = updateUser(delivery);
                    break;
                case "DeleteUser":
                    response = deleteUser(delivery);
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

    /**
     * This method processes the creation of a user through the RPC channel.
     * 
     * Normally this situation can occur when a user is registering in our service,
     * therefore this method will be important for our operations.
     * 
     * @param delivery The rpc petition.
     * @return The response message.
     * @throws Exception If an exception occurs.
     */
    public byte[] createUser(Delivery delivery) throws Exception {
        // Prepare the database connection.
        UsersConn connection = Connections.getInstance().acquireUsers();

        // Prepare the return message.
        byte[] message = null;

        // Obtain a Protocol Schemas
        Protocol protocol = Protocol.parse(this.getClass().getResourceAsStream("CreateUser.avpr"));

        try {
            // Prepare the generic schema reader.
            DatumReader<GenericRecord> consumer = new GenericDatumReader<>(protocol.getType("Request"));

            // Decode the binary message into a readable for the reader.
            ByteArrayInputStream consumedByteArray = new ByteArrayInputStream(delivery.getBody());
            Decoder consumedDecoder = DecoderFactory.get().binaryDecoder(consumedByteArray, null);

            // Convert the Decoder data to GenericRecord data.
            GenericRecord consumedDatum = consumer.read(null, consumedDecoder);

            // Create a user handled by RPC interface.
            User user = new User.Builder(null).setName((String) consumedDatum.get("name"))
                    .setSurname((String) consumedDatum.get("surname")).setEmail((String) consumedDatum.get("email"))
                    .setPassword((String) consumedDatum.get("password"))
                    .setTelephone((String) consumedDatum.get("telephone"))
                    .setBirthday(DatatypeConverter.parseDateTime((String) consumedDatum.get("birthday")).getTime())
                    .setCountry(Enum.valueOf(Country.class, (String) consumedDatum.get("country")))
                    .setAddress((String) consumedDatum.get("address"))
                    .setAddressInformation((String) consumedDatum.get("addressInformation")).build();

            // Set the user in the database.
            connection.create(user);

            // Prepare the return message.
            GenericRecord response = new GenericData.Record(protocol.getType("Response"));
            response.put("status", Status.OK.getStatusCode());
            response.put("message", null);

            // Prepare the writer for generate a response.
            DatumWriter<GenericRecord> responser = new GenericDatumWriter<>(protocol.getType("Response"));

            // Prepare the output stream.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BinaryEncoder binaryEncoder = EncoderFactory.get().binaryEncoder(out, null);

            // Write the message into output stream.
            responser.write(response, binaryEncoder);

            // Flush the encoder buffer.
            binaryEncoder.flush();

            // Return the data into the router.
            message = out.toByteArray();
        } catch (Exception e) {
            // Prepare the return message.
            GenericRecord response = new GenericData.Record(protocol.getType("Response"));
            response.put("status", Status.INTERNAL_SERVER_ERROR.getStatusCode());
            response.put("message", null);

            // Prepare the writer for generate a response.
            DatumWriter<GenericRecord> responser = new GenericDatumWriter<>(protocol.getType("Response"));

            // Prepare the output stream.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BinaryEncoder binaryEncoder = EncoderFactory.get().binaryEncoder(out, null);

            // Write the message into output stream.
            responser.write(response, binaryEncoder);

            // Flush the encoder buffer.
            binaryEncoder.flush();

            // Return the data into the router.
            message = out.toByteArray();
        } finally {
            // Release the connection with the database.
            Connections.getInstance().releaseUsers(connection);
        }

        return message;
    }

    public byte[] readUser(Delivery delivery) {
        return null;
    }

    public byte[] updateUser(Delivery delivery) {
        return null;
    }

    public byte[] deleteUser(Delivery delivery) {
        return null;
    }

}