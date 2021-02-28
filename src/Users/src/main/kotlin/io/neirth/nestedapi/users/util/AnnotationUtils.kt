package io.neirth.nestedapi.users.util

import com.rabbitmq.client.*
import io.neirth.nestedapi.users.util.annotation.RpcMessage

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

import javax.lang.model.element.ExecutableElement

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes("io.neirth.nestedapi.users.controller")
class AnnotationUtils : AbstractProcessor() {
    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (roundEnv != null) {
            // Prepare a new connection factory object
            val connFactory = ConnectionFactory();

            // Set the URI for attach to broker
            connFactory.setUri(System.getenv("RABBITMQ_AMQP_URI"))

            // Generate a new connection
            val conn : Connection = connFactory.newConnection()

            // Retreive the annotations with RpcMessage
            for (element : Element in roundEnv.getElementsAnnotatedWith(RpcMessage::class.java)) {
                if (element.kind == ElementKind.METHOD) {
                    // We obtain the annotation
                    val rpcMessage : RpcMessage = element.getAnnotation(RpcMessage::class.java)

                    // And get the values
                    val queue : String = rpcMessage.queue
                    val topic : String = rpcMessage.topic

                    val execElement : ExecutableElement = element as ExecutableElement

                    // This is the moment to start a new conneciton.
                    conn.createChannel().use {
                        // We declared the new topic
                        it.exchangeDeclare(topic, "topic")

                        // Obtain a generated queue name
                        val queueName : String = it.queueDeclare().queue

                        // Bind the queue with routing key
                        it.queueBind(queueName, topic, "$topic.$queue")

                        // Generate a nothing callback
                        val nothing = CancelCallback { consumerTag -> }

                        // Generate a deliver callback
                        val callback = DeliverCallback { consumerTag: String?, delivery: Delivery ->
                            // Get the bytearray of delivery
                            val data : ByteArray = delivery.getBody()

                            // Deserialize the data

                            // We try to call the annoted method with deserialized data

                            // Serialize the response

                            // Send the response
                        }

                        // Start a basic consume
                        it.basicConsume(queueName, true, callback, nothing)
                    }
                }
            }

            // If the operation is complete, return true
            return true
        } else {
            // If the operation is false, return false
            return false
        }
    }

}