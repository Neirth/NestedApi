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