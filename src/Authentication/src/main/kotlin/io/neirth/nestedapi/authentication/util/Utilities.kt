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
package io.neirth.nestedapi.authentication.util

import io.jsonwebtoken.SignatureAlgorithm
import org.eclipse.microprofile.config.ConfigProvider
import java.security.Key
import java.util.logging.Logger
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.mail.Message

import javax.mail.Session
import javax.mail.internet.InternetAddress

import javax.mail.internet.MimeMessage

val signingKey : Key = SecretKeySpec(
    DatatypeConverter.parseBase64Binary(System.getenv("LOGIN_KEY")),
    SignatureAlgorithm.HS512.jcaName
)

val loggerSystem: Logger = Logger.getLogger("Authentication Module")
var sessionMail : Session? = null

/**
 * Static method for transform the www encoded variables into map
 *
 * @param formEncoded String encoded with www format
 * @return Map with converted values
 */
fun parseFormEncoded(formEncoded: String): Map<String, String> {
    // Prepare the hashmap
    val formMap: MutableMap<String, String> = HashMap()

    // For each variable, map it into the hashmap
    for (pair in formEncoded.split("&").toTypedArray()) {
        // Obtain the index of key
        val index = pair.indexOf("=")

        // Obtain the key and the value
        val key: String = URLDecoder.decode(pair.substring(0, index), StandardCharsets.UTF_8)
        val value: String = URLDecoder.decode(pair.substring(index + 1, pair.length), StandardCharsets.UTF_8)

        // Set these values into the hashmap
        formMap[key] = value
    }

    // Return the hashmap
    return formMap
}

/**
 * Method for simplify the process to send emails through the Internet
 *
 * @param to Who was receive the email
 * @param subject The email subject
 * @param title The html template title
 * @param message The html template message
 */
fun sendEmail(to: String, subject: String, title: String, message: String) {
    if (ConfigProvider.getConfig().getValue("mail.enable.support", Boolean::class.java)) {
        if (sessionMail == null) {
            // Instantiate a Properties object
            val properties = Properties()

            // Map the environment variables into properties
            properties["mail.smtp.host"] = ConfigProvider.getConfig().getValue("mail.smtp.host", String::class.java)
            properties["mail.smtp.starttls.enable"] = ConfigProvider.getConfig().getValue("mail.smtp.starttls.enable", Boolean::class.java)
            properties["mail.smtp.port"] = ConfigProvider.getConfig().getValue("mail.smtp.port", Int::class.java)
            properties["mail.smtp.mail.sender"] = ConfigProvider.getConfig().getValue("mail.smtp.mail.sender", String::class.java)
            properties["mail.smtp.user"] = ConfigProvider.getConfig().getValue("mail.smtp.user", String::class.java)
            properties["mail.smtp.auth"] = ConfigProvider.getConfig().getValue("mail.smtp.auth", Boolean::class.java)

            // Instantiate the Session Mail with Properties
            sessionMail = Session.getDefaultInstance(properties)
        }

        // Prepare the email object
        val mail = MimeMessage(sessionMail)

        // Set from & to properties
        mail.setFrom(InternetAddress(sessionMail?.getProperty("mail.smtp.mail.sender")))
        mail.addRecipient(Message.RecipientType.TO, InternetAddress(to))

        // Set the subject
        mail.subject = subject

        // Set the email content
        mail.setText(String.format(Files.readString(Path.of("Templates/mail.html")), title, message))

        // Send email through the Internet
        sessionMail!!.getTransport("smtp").use {
            it.connect(sessionMail?.getProperty("mail.smtp.user"), sessionMail?.getProperty("mail.smtp.password"))
            it.sendMessage(mail, mail.allRecipients)
        }
    }
}
