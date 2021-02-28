package io.neirth.nestedapi.users

import io.neirth.nestedapi.users.util.initRpcQueues
import io.neirth.nestedapi.users.util.loggerSystem
import io.quarkus.runtime.Quarkus
import io.quarkus.runtime.QuarkusApplication
import java.util.logging.Level

fun main(args: Array<String>) {
    Quarkus.run(InitApp::class.java, *args)
}

class InitApp : QuarkusApplication {
    override fun run(vararg args: String?): Int {
        try {
            // Notify the init of rpc queues
            loggerSystem.log(Level.INFO, "Starting RPC Queues...")

            // Init RPC Queues
            initRpcQueues()

            // Notify the started rpc queues
            loggerSystem.log(Level.INFO, "Started RPC Queues!")

            // Notify the server is ready
            loggerSystem.log(Level.INFO, "Ready to receive requests!")

            // Wait of stop the system
            Quarkus.waitForExit()

            // Notify of stopping the server
            loggerSystem.log(Level.INFO, "Stopping the server...")
        } catch (e: Exception) {
            loggerSystem.log(Level.SEVERE, "An exception has occurred, $e")
            //e.printStackTrace()
        }

        return 0
    }
}