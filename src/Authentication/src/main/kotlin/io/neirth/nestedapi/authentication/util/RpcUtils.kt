package io.neirth.nestedapi.authentication.util

import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonNode
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.*
import de.undercouch.bson4jackson.BsonFactory
import io.neirth.nestedapi.authentication.util.annotation.RpcMessage
import java.util.*
import kotlin.collections.ArrayList
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

/**
 * Method for init the RPC Queues
 */
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

/**
 * Method for send RPC Messages throw the network without schema
 * @param topicRedirect The routing key
 * @param obj The Json Node Obj
 * @return The network response or null if nothing is passed
 */
fun sendMessage(topicRedirect: String, obj: JsonNode) : JsonNode? {
    // Instance a connection factory
    val connFactory = ConnectionFactory()

    // Set the URI of RabbitMQ Broker
    connFactory.setUri(System.getenv("RABBITMQ_AMQP_URI"))

    // Instance a connection object
    val conn : Connection = connFactory.newConnection()

    // Create channel
    conn.createChannel().use {
        // Set the new Correlation ID
        val corrId : String = UUID.randomUUID().toString()

        // Declare a Queue to receive the response
        val replyTo : String = it.queueDeclare().queue

        // Set AMQP properties
        val props : AMQP.BasicProperties = AMQP.BasicProperties().builder()
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
        it.basicPublish(routing[0], topicRedirect, props, output.toByteArray())

        // Get a Blocking Queue with the response
        val response : BlockingQueue<JsonNode> = ArrayBlockingQueue(1)

        // Generate a nothing callback
        val nothing = CancelCallback {  }

        // Generate a response callback
        val callback = DeliverCallback { consumerTag, delivery ->
            if (delivery.properties.correlationId == corrId) {
                response.offer(mapper.readTree(ByteArrayInputStream(delivery.body)))
            }
        }

        // Wait to the server response
        it.basicConsume(replyTo, true, callback, nothing)

        // Set a timeout for the response
        response.poll(1, TimeUnit.SECONDS)

        // Wait the response
        return response.take()
    }
}