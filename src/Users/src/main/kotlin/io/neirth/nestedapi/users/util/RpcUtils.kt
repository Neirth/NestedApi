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
package io.neirth.nestedapi.users.util

import com.rabbitmq.client.*
import io.neirth.nestedapi.users.util.annotation.RpcMessage

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

import de.undercouch.bson4jackson.BsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.*

fun initRpcQueues() {
    // Load Properties from XML
    val props = Properties()
    props.loadFromXML(ClassLoader.getSystemResourceAsStream("META-INF/rpc-classes.xml"))

    // Prepare the arraylist
    val clazzArr : ArrayList<Class<*>> = ArrayList()

    // Scan all RPC Market classes
    for (clazz in props.getProperty("rpc.classes").split(",")) {
        clazzArr.add(Class.forName(clazz))
    }

    // Prepare a new connection factory object
    val connFactory = ConnectionFactory()

    // Set the URI for attach to broker
    connFactory.setUri(System.getenv("RABBITMQ_AMQP_URI"))

    // Initialize all classes
    for (clazz in clazzArr) {
        for (method in clazz.methods) {
            if (method.isAnnotationPresent(RpcMessage::class.java)) {
                try {
                    // We obtain the annotation
                    val rpcMessage: RpcMessage = method.getAnnotation(RpcMessage::class.java)

                    // And get the values
                    val queue: String = rpcMessage.queue
                    val topic: String = rpcMessage.topic

                    // Generate a new connection
                    val conn: Connection = connFactory.newConnection()

                    // This is the moment to start a new connection.
                    conn.createChannel().use {
                        // We declared the new topic
                        it.exchangeDeclare(topic, "topic")

                        // Obtain a generated queue name
                        val queueName: String = it.queueDeclare().queue

                        // Bind the queue with routing key
                        it.queueBind(queueName, topic, "$topic.$queue")

                        // Generate a nothing callback
                        val nothing = CancelCallback { }

                        // Generate a deliver callback
                        val callback = DeliverCallback { _: String?, delivery: Delivery ->
                            // Get the bytearray of delivery
                            val data: ByteArray = delivery.body

                            // Prepare the object mapper
                            val mapper = ObjectMapper(BsonFactory())

                            // Check if exist parameters
                            val result: Any = if (method.parameters.isNotEmpty()) {
                                // Get the parameter type
                                val type: Class<*> = method.parameters[0].type

                                // Deserialize the data
                                val entity: Any? = mapper.readValue(ByteArrayInputStream(data), type)

                                // We try to call the annotated method with deserialized data
                                method.invoke(clazz.newInstance(), entity)
                            } else {
                                // We try to call the annotated method without serialized data
                                method.invoke(clazz.newInstance())
                            }

                            // Prepare the byte buffer stream
                            val output = ByteArrayOutputStream()

                            // Serialize the response
                            mapper.writeValue(output, result)

                            // Prepare the properties of the response.
                            val replyProps: AMQP.BasicProperties = AMQP.BasicProperties.Builder().correlationId(delivery.properties.correlationId).build()

                            // Publish the response into the private queue and sets the acknowledge.
                            it.basicPublish("", delivery.properties.replyTo, replyProps, output.toByteArray())
                            it.basicAck(delivery.envelope.deliveryTag, false)
                        }

                        // Start a basic consume
                        it.basicConsume(queueName, true, callback, nothing)
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
}
