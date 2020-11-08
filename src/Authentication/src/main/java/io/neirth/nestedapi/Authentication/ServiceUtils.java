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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.neirth.nestedapi.Authentication;

// Used libraries from Java Standard.
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

// Used libraries from Java Enterprise.
import javax.crypto.spec.SecretKeySpec;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;

// Used library for logging the server events.
import org.jboss.logging.Logger;
import org.jboss.resteasy.spi.HttpRequest;

// Used libraries for process the jwt token.
import io.jsonwebtoken.SignatureAlgorithm;

public class ServiceUtils {
    // The instance of logger.
    private static final Logger loggerSystem = Logger.getLogger(ServiceApp.class);

    // The token private key.
    private static final Key privateKey = new SecretKeySpec(DatatypeConverter.parseBase64Binary(System.getenv("LOGIN_KEY")),
                                                            SignatureAlgorithm.HS512.getJcaName());

    // The email session.
    private static Session session = null;

    // The callback with custom procedures.
    public interface RestCallback {
        ResponseBuilder run() throws Exception;
    }

    /**
     * Method that process the request received from REST API and generate a
     * response in the json format.
     * 
     * This method, with the corresponding RestCallback to do the specific tasks,
     * retrieves the information from the body and executes the callback lambda, the
     * message returned is a HTTP response with Json Body.
     * 
     * @param req         The headers of http request.
     * @param paramId     The paramId of the requested object.
     * @param body        The body of http request.
     * @param callback    The lambda callback
     * @return The response object.
     */
    public static Response processRequest(HttpRequest req, Long paramId, String body, RestCallback callback) {
        // Initialize the response builder.
        ResponseBuilder response = null;

        try {
            // Run the callback and get the response.
            response = callback.run();
        } catch (Exception e) {
            // Write the exception in the log.
            writeServerException(e);

            // Build the json error response.
            JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
            jsonResponse.add("error", "server_error");
            jsonResponse.add("error_description", "An error has occurred on the server while processing your request.");

            // Create error response.
            response = Response.status(Status.INTERNAL_SERVER_ERROR).entity(jsonResponse.build().toString()).encoding(MediaType.APPLICATION_JSON);
        }

        // Throws the response.
        return response.build();
    }

    /**
     * Method to write the exception in the logs.
     * 
     * It's useful for write the stack trace in debug mode or only the error in
     * production.
     * 
     * @param e The exception catched.
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
     * Method for parse the url www encoded variables to Map<String, String>.
     * 
     * @param formEncoded URL WWW encoded variables.
     * @return The map with key value variables.
     */
    public static Map<String, String> parseFormEncoded(String formEncoded) {
        // Instance the map.
        Map<String, String> formMap = new HashMap<>();

        // For each the key-value variables.
        for (String pair: formEncoded.split("&")) {
            // Obtain the index.
            int index = pair.indexOf("=");

            // Set the key-value pair into the map.
            formMap.put(URLDecoder.decode(pair.substring(0, index), StandardCharsets.UTF_8), 
                        URLDecoder.decode(pair.substring(index + 1), StandardCharsets.UTF_8));
        }
        
        // Return the map.
        return formMap;
    }

    /**
     * Private method that returns the email session.
     * 
     * @return The email session.
     */
    private static Session getSession() {
        if (session == null) {
            // Initialize the properties object
            Properties properties = new Properties();

            // Set the values for property object.
            properties.put("mail.smtp.host", System.getenv("SMTP_HOST"));
            properties.put("mail.smtp.port", System.getenv("SMTP_PORT"));
            properties.put("mail.smtp.user", System.getenv("SMTP_USER"));
            properties.put("mail.smtp.mail.sender", System.getenv("SMTP_SENDER"));
            properties.put("mail.smtp.auth", Boolean.parseBoolean(System.getenv("SMTP_AUTH")));
            properties.put("mail.smtp.starttls.enable", Boolean.parseBoolean(System.getenv("SMTP_STARTTTLS")));
            
            // Get the default instance.
            session = Session.getDefaultInstance(properties);
        }

        return session;
    }

    /**
     * Wrapped method to send emails to the registered users.
     * 
     * @param address The email address.
     * @param subject The email subject.
     * @param body    The email body.
     */
    public static void sendMail(String address, String subject, String body) throws MessagingException {
        // Initialize the email object.
        MimeMessage message = new MimeMessage(getSession());

        // Set the email properties.
        message.setFrom(new InternetAddress(System.getenv("SMTP_SENDER")));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
        message.setSubject(subject);
        message.setText(body);

        // Send the email to the email server.
        try (Transport t = session.getTransport("smtp")) {
            t.connect(System.getenv("SMTP_USER"), System.getenv("SMTP_PASSWORD"));
            t.sendMessage(message, message.getAllRecipients());
        }
    }

    /**
     * Method to get the sign token key.
     * 
     * @return The sign token key.
     */
    public static Key getKey() {
        return privateKey;
    }

    /**
     * Method to access the event log of this module.
     * 
     * @return The logger instance.
     */
    public static Logger getLoggerSystem() {
        return loggerSystem;
    }
}