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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.*
import de.undercouch.bson4jackson.BsonFactory
import io.neirth.nestedapi.authentication.util.annotation.RpcMessage
import io.quarkus.arc.Unremovable
import io.quarkus.runtime.StartupEvent
import org.eclipse.microprofile.context.ManagedExecutor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.ConnectException
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.context.Dependent
import javax.enterprise.context.control.ActivateRequestContext
import javax.enterprise.event.Observes
import javax.enterprise.inject.spi.CDI
import javax.transaction.Transactional
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

@ApplicationScoped
class RpcUtils(var executor: ManagedExecutor) {
    /**
     * Method for init the RPC Queues
     */
    internal fun onStart(@Observes ev: StartupEvent) {
        loggerSystem.log(Level.INFO, "Starting RPC Queues...")

        try {
            // Load Properties from XML
            val props = Properties()
            props.loadFromXML(ClassLoader.getSystemResourceAsStream("META-INF/rpc-classes.xml"))

            // Prepare the arraylist
            val clazzArr: ArrayList<Class<*>> = ArrayList()

            // Scan all RPC Market classes
            for (clazz in props.getProperty("rpc.classes").split(",")) {
                clazzArr.add(Class.forName(clazz))
            }

            // Prepare a new connection factory object
            val connFactory = ConnectionFactory()

            // Set the URI for attach to broker
            connFactory.setUri(System.getenv("RABBITMQ_AMQP_URI"))

            // Initialize all classes
            processClasses(connFactory, clazzArr)

            loggerSystem.log(Level.INFO, "Started RPC Queues!")
        } catch (e: ConnectException) {
            loggerSystem.log(Level.SEVERE, "Could not connect to the message broker!")
            exitProcess(-1)
        }
    }

    /**
     * Method for process all classes annotated with @RpcMessage
     */
    internal fun processClasses(connFactory: ConnectionFactory, clazzArr: ArrayList<Class<*>>) {
        for (clazz in clazzArr) {
            for (method in clazz.methods) {
                if (method.isAnnotationPresent(RpcMessage::class.java)) {
                    // We obtain the annotation
                    val rpcMessage: RpcMessage = method.getAnnotation(RpcMessage::class.java)

                    // And get the values
                    val queue: String = rpcMessage.queue
                    val topic: String = rpcMessage.topic

                    // Generate a new connection
                    val channel = connFactory.newConnection().createChannel()

                    // We declared the new topic
                    channel.exchangeDeclare(topic, "topic")

                    // Obtain a generated queue name
                    val queueName: String = UUID.randomUUID().toString()

                    // Declare a queue with options
                    channel.queueDeclare(queueName, false, true, false, null)

                    // Bind the queue with routing key
                    channel.queueBind(queueName, topic, queue)

                    // Generate a deliver callback
                    val callback = DeliverCallback { _: String?, delivery: Delivery ->
                        // Instance bean with CDI
                        val deliveryRunnable: DeliveryCallback = CDI.current().select(DeliveryCallback::class.java).get()

                        // Set the common properties
                        deliveryRunnable.delivery = delivery
                        deliveryRunnable.channel = channel
                        deliveryRunnable.method = method
                        deliveryRunnable.clazz = clazz

                        // Execute the thread in thread pool
                        executor.execute(deliveryRunnable)
                    }

                    // Start a basic consume
                    channel.basicConsume(queueName, false, callback, CancelCallback { })
                }
            }
        }
    }

    /**
     * Method for instance the callback instructions when receive a RPC Message
     */
    @Dependent
    @Unremovable
    internal class DeliveryCallback : Runnable {
        lateinit var delivery: Delivery
        lateinit var channel: Channel
        lateinit var method: Method
        lateinit var clazz: Class<*>

        @Transactional
        @ActivateRequestContext
        override fun run() {
            try {
                // Prepare the object mapper
                val mapper = ObjectMapper(BsonFactory())

                // Get the instance
                val instance = CDI.current().select(clazz).get()

                // Check if exist parameters
                val result: Any = if (method.parameters.isNotEmpty()) {
                    // Get the parameter type
                    val type: Class<*> = method.parameters[0].type

                    // Deserialize the data
                    val entity: Any? = mapper.readValue(delivery.body, type)

                    // We try to call the annotated method with deserialized data
                    method.invoke(instance, entity)
                } else {
                    // We try to call the annotated method without serialized data
                    method.invoke(instance)
                }

                // Prepare the byte buffer stream
                val output = ByteArrayOutputStream()

                // Serialize the response
                mapper.writeValue(output, result)

                // Prepare the properties of the response.
                val replyProps: AMQP.BasicProperties = AMQP.BasicProperties.Builder().correlationId(delivery.properties.correlationId).build()

                // Publish the response into the private queue and sets the acknowledge.
                channel.basicPublish("", delivery.properties.replyTo, replyProps, output.toByteArray())
                channel.basicAck(delivery.envelope.deliveryTag, true)
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        /**
         * Method for send RPC Messages throw the network without schema
         * @param topicRedirect The routing key
         * @param obj The Json Node Obj
         * @return The network response or null if nothing is passed
         */
        fun sendMessage(topicRedirect: String, obj: JsonNode): JsonNode? {
            // Instance a connection factory
            val connFactory = ConnectionFactory()

            // Set the URI of RabbitMQ Broker
            connFactory.setUri(System.getenv("RABBITMQ_AMQP_URI"))

            // Create channel
            val channel = connFactory.newConnection().createChannel()
            // Set the new Correlation ID
            val corrId: String = UUID.randomUUID().toString()

            // Declare a Queue to receive the response
            val replyTo: String = UUID.randomUUID().toString()

            // Declare a queue with options
            channel.queueDeclare(replyTo, false, true, true, null)

            // Set AMQP properties
            val props: AMQP.BasicProperties = AMQP.BasicProperties().builder()
                .correlationId(corrId).replyTo(replyTo)
                .build()

            // Set the bson factory
            val bsonFactory = BsonFactory()

            // Set the output array
            val output = ByteArrayOutputStream()

            // Prepare the object mapper with bson encoding
            val mapper = ObjectMapper(bsonFactory)

            // Map the passed object
            mapper.writeTree(bsonFactory.createGenerator(output), obj)

            // Get the routing key
            val routing: Array<String> = topicRedirect.split(".").toTypedArray()

            // Publish the RPC message to the topic
            channel.basicPublish(routing[0], routing[1], props, output.toByteArray())

            // Get a Blocking Queue with the response
            val response: BlockingQueue<JsonNode> = ArrayBlockingQueue(1)

            // Generate a nothing callback
            val nothing = CancelCallback { }

            // Generate a response callback
            val callback = DeliverCallback { _, delivery ->
                if (delivery.properties.correlationId == corrId) {
                    response.offer(mapper.readTree(ByteArrayInputStream(delivery.body)))
                }
            }

            // Wait to the server response
            channel.basicConsume(replyTo, true, callback, nothing)

            // Set a timeout for the response and wait
            return response.poll(1, TimeUnit.SECONDS)
        }
    }
}
